package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractExperiment<T>(
        val name: String,
        val raiseOnMismatch: Boolean = false,
        val metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        val context: Map<String, Any> = emptyMap(),
        val comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        val sampleFactory: SampleFactory = SampleFactory()
) {
    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val controlTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "control")
    private val candidateTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "candidate")
    private val mismatchCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "mismatch")
    private val candidateExceptionCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "candidate.exception")
    private val totalCount: Counter = metrics.counter(NAMESPACE_PREFIX, name, "total")

    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)
    constructor(name: String, metrics: MetricsProvider<*>) : this(name, false, metrics)

    companion object {
        const val NAMESPACE_PREFIX = "scientist"
        private val LOGGER: Logger = LoggerFactory.getLogger(AbstractExperiment::class.java)
    }

    protected fun scrapCandidate(): Observation<T> =
            scrap("candidate")

    protected fun executeCandidate(candidate: () -> T?) =
            execute("candidate", candidateTimer, candidate, false)

    protected fun executeControl(control: () -> T?) =
            execute("control", controlTimer, control, true)

    protected fun countExceptions(observation: Observation<T>, exceptions: Counter) {
        if (observation.exception != null) {
            exceptions.increment()
        }
    }

    protected fun execute(name: String, timer: Timer, function: () -> T?, shouldThrow: Boolean): Observation<T> {
        val observation = Observation<T>(name, timer)
        observation.time(function)
        val exception = observation.exception
        if (exception != null && shouldThrow) {
            throw exception
        }
        return observation
    }

    protected fun scrap(name: String): Observation<T> {
        val observation: Observation<T> = Observation(name)
        observation.status = ObservationStatus.SCRAPPED
        return observation
    }

    open fun runIf(): Boolean {
        return true
    }

    open fun enabled(): Boolean {
        return true
    }

    open val isAsync: Boolean
        get() = true

    fun compare(control: Observation<T>, candidate: Observation<T>): ComparisonResult {
        countExceptions(candidate, candidateExceptionCount)
        return if (candidate.exception != null) {
            ComparisonResult("Candidate threw an exception.")
        } else {
            LOGGER.info("Comparing {} with {}", control.value, candidate.value)
            comparator.invoke(control.value, candidate.value)
        }.apply {
            LOGGER.info("Compared...match={}", this.matches)
            totalCount.increment()
            if (!matches) {
                mismatchCount.increment()
            }
        }
    }

}
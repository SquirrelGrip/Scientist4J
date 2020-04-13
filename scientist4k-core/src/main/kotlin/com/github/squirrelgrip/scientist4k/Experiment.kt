package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

open class Experiment<T>(
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
    private val publishers = mutableListOf<Publisher<T>>()
    private val controlTimer: Timer
    private val candidateTimer: Timer
    private val mismatchCount: Counter
    private val candidateExceptionCount: Counter
    private val totalCount: Counter

    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)
    constructor(name: String, metrics: MetricsProvider<*>) : this(name, false, metrics)

    init {
        controlTimer = metrics.timer(NAMESPACE_PREFIX, name, "control")
        candidateTimer = metrics.timer(NAMESPACE_PREFIX, name, "candidate")
        mismatchCount = metrics.counter(NAMESPACE_PREFIX, name, "mismatch")
        candidateExceptionCount = metrics.counter(NAMESPACE_PREFIX, name, "candidate.exception")
        totalCount = metrics.counter(NAMESPACE_PREFIX, name, "total")
    }

    fun addPublisher(publisher: Publisher<T>) {
        publishers.add(publisher)
    }

    fun removePublisher(publisher: Publisher<T>) {
        publishers.remove(publisher)
    }

    open fun run(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        return if (isAsync) {
            runAsync(control, candidate, sample)
        } else {
            runSync(control, candidate, sample)
        }
    }

    open fun runSync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        val controlObservation: Observation<T> = executeControl(control)
        val candidateObservation = if (runIf() && enabled()) {
            executeCandidate(candidate)
        } else {
            null
        }
        if (candidateObservation != null) {
            publishResult(candidateObservation, controlObservation, sample).handleComparisonMismatch()
        }
        return controlObservation.value
    }

    open fun runAsync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()) =
            runBlocking {
                val deferredControlObservation = GlobalScope.async { executeControl(control) }
                val deferredCandidateObservation = if (runIf() && enabled()) {
                    GlobalScope.async { executeCandidate(candidate) }
                } else {
                    null
                }

                val controlObservation = deferredControlObservation.await()
                if (deferredCandidateObservation != null) {
                    val deferred = GlobalScope.async { publishAsync(deferredCandidateObservation, controlObservation, sample) }
                    if (raiseOnMismatch) {
                        deferred.await().handleComparisonMismatch()
                    }
                }
                controlObservation.value
            }

    private suspend fun publishAsync(deferredCandidateObservation: Deferred<Observation<T>>, controlObservation: Observation<T>, sample: Sample = sampleFactory.create()): Result<T> {
        deferredCandidateObservation.await().also { candidateObservation ->
            return publishResult(candidateObservation, controlObservation, sample)
        }
    }

    private fun publishResult(candidateObservation: Observation<T>, controlObservation: Observation<T>, sample: Sample = sampleFactory.create()): Result<T> {
        countExceptions(candidateObservation, candidateExceptionCount)
        return Result(this@Experiment, controlObservation, candidateObservation, context, sample).apply {
            publish(this)
        }
    }

    private fun executeCandidate(candidate: () -> T?) =
            execute("candidate", candidateTimer, candidate, false)

    private fun executeControl(control: () -> T?) =
            execute("control", controlTimer, control, true)

    private fun countExceptions(observation: Observation<T>, exceptions: Counter) {
        if (observation.exception != null) {
            exceptions.increment()
        }
    }

    private fun execute(name: String, timer: Timer, function: () -> T?, shouldThrow: Boolean): Observation<T> {
        val observation = Observation<T>(name, timer)
        observation.time {
            try {
                observation.setValue(function.invoke())
            } catch (e: Exception) {
                observation.setException(e)
            }
        }
        val exception = observation.exception
        if (exception != null && shouldThrow) {
            throw exception
        }
        return observation
    }

    fun compare(control: Observation<T>, candidate: Observation<T>): ComparisonResult {
        return if (candidate.exception != null) {
            ComparisonResult("Candidate threw an exception.")
        } else {
            comparator.invoke(control.value, candidate.value)
        }.apply {
            totalCount.increment()
            if (!matches) {
                mismatchCount.increment()
            }
        }
    }

    open fun runIf(): Boolean {
        return true
    }

    open fun enabled(): Boolean {
        return true
    }

    open val isAsync: Boolean
        get() = true

    open fun publish(result: Result<T>) {
        publishers.forEach { it.publish(result) }
        result.sample.published.set(true)
    }

    companion object {
        private const val NAMESPACE_PREFIX = "scientist"
    }

}
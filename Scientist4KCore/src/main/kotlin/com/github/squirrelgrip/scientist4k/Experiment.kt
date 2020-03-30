package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.function.BiFunction

open class Experiment<T>(
        name: String,
        val context: Map<String, Any> = emptyMap(),
        val raiseOnMismatch: Boolean,
        metricsProvider: MetricsProvider<*> = DropwizardMetricsProvider(),
        val comparator: BiFunction<T?, T?, Boolean> = BiFunction { a: T?, b: T? -> a == b }
) {
    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val controlTimer: Timer
    private val candidateTimer: Timer
    private val mismatchCount: Counter
    private val candidateExceptionCount: Counter
    private val totalCount: Counter

    constructor(metricsProvider: MetricsProvider<*>) : this("Experiment", metricsProvider)
    constructor(name: String, metricsProvider: MetricsProvider<*>) : this(name, false, metricsProvider)
    constructor(name: String, context: Map<String, Any>, metricsProvider: MetricsProvider<*>) : this(name, context, false, metricsProvider)
    constructor(name: String, raiseOnMismatch: Boolean, metricsProvider: MetricsProvider<*>) : this(name, mapOf<String, Any>(), raiseOnMismatch, metricsProvider)

    init {
        controlTimer = metricsProvider.timer(NAMESPACE_PREFIX, name, "control")
        candidateTimer = metricsProvider.timer(NAMESPACE_PREFIX, name, "candidate")
        mismatchCount = metricsProvider.counter(NAMESPACE_PREFIX, name, "mismatch")
        candidateExceptionCount = metricsProvider.counter(NAMESPACE_PREFIX, name, "candidate.exception")
        totalCount = metricsProvider.counter(NAMESPACE_PREFIX, name, "total")
    }

    open fun run(control: () -> T?, candidate: () -> T?, sample: Sample = Sample()): T? {
        return if (isAsync) {
            runAsync(control, candidate, sample)
        } else {
            runSync(control, candidate, sample)
        }
    }

    open fun runSync(control: () -> T?, candidate: () -> T?, sample: Sample = Sample()): T? {
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

    open fun runAsync(control: () -> T?, candidate: () -> T?, sample: Sample = Sample()) =
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

    private suspend fun publishAsync(deferredCandidateObservation: Deferred<Observation<T>>, controlObservation: Observation<T>, sample: Sample = Sample()): Result<T> {
        deferredCandidateObservation.await().also { candidateObservation ->
            return publishResult(candidateObservation, controlObservation, sample)
        }
    }

    private fun publishResult(candidateObservation: Observation<T>, controlObservation: Observation<T>, sample: Sample = Sample()): Result<T> {
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

    private fun compareResults(control: T?, candidate: T?): Boolean {
        return comparator.apply(control, candidate)
    }

    fun compare(control: Observation<T>, candidate: Observation<T>): Boolean {
        val resultsMatch = candidate.exception == null && compareResults(control.value, candidate.value)
        totalCount.increment()
        if (!resultsMatch) {
            mismatchCount.increment()
            return false
        }
        return true
    }

    open fun publish(result: Result<T>) {}

    open fun runIf(): Boolean {
        return true
    }

    open fun enabled(): Boolean {
        return true
    }

    open val isAsync: Boolean
        get() = true

    companion object {
        private const val NAMESPACE_PREFIX = "scientist"
    }
}
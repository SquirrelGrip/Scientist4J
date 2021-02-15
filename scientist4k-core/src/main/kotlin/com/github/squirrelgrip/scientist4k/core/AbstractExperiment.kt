package com.github.squirrelgrip.scientist4k.core

import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.*
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservationType.*
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

abstract class AbstractExperiment<T>(
    val experimentConfiguration: ExperimentConfiguration,
    val comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    val eventBus: EventBus = DEFAULT_EVENT_BUS,
) {
    val name: String
        get() = experimentConfiguration.name

    val sampleThreshold: Int
        get() = experimentConfiguration.sampleThreshold

    val experimentOptions: EnumSet<ExperimentOption>
        get() = experimentConfiguration.experimentOptions

    val metricsProvider: MetricsProvider<*>
        get() = experimentConfiguration.metricsProvider

    val sampleFactory: SampleFactory
        get() = experimentConfiguration.sampleFactory

    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val controlTimer: Timer = metricsProvider.timer(NAMESPACE_PREFIX, name, "control")
    private val candidateTimer: Timer = metricsProvider.timer(NAMESPACE_PREFIX, name, "candidate")
    private val mismatchCount: Counter = metricsProvider.counter(NAMESPACE_PREFIX, name, "mismatch")
    private val candidateExceptionCount: Counter = metricsProvider.counter(NAMESPACE_PREFIX, name, "candidate.exception")
    private val totalCount: Counter = metricsProvider.counter(NAMESPACE_PREFIX, name, "total")

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AbstractExperiment::class.java)
        const val NAMESPACE_PREFIX = "scientist"
        val DEFAULT_EVENT_BUS: EventBus = EventBus()
    }

    open fun isSync(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.SYNC) || experimentOptions.contains(ExperimentOption.SYNC)

    open fun isReturnCandidate(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.RETURN_CANDIDATE) || experimentOptions.contains(ExperimentOption.RETURN_CANDIDATE)

    open fun isRaiseOnMismatch(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.RAISE_ON_MISMATCH) || experimentOptions.contains(ExperimentOption.RAISE_ON_MISMATCH)

    open fun isDisabled(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.DISABLED) || experimentOptions.contains(ExperimentOption.DISABLED) || sample.exceedsThreshold(sampleThreshold)

    open fun isWithholdPublication(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.WITHHOLD_PUBLICATION) || experimentOptions.contains(ExperimentOption.WITHHOLD_PUBLICATION) || sample.exceedsThreshold(sampleThreshold)

    open fun isPublishable(sample: Sample) = !isWithholdPublication(sample)

    protected fun executeCandidate(
        candidate: () -> T?,
        sample: Sample
    ): ExperimentObservation<T> =
        if (isDisabled(sample) && !isReturnCandidate(sample)) {
            scrap(CANDIDATE)
        } else {
            execute(
                CANDIDATE,
                candidateTimer,
                candidate,
                isReturnCandidate(sample)
            )
        }

    protected fun executeControl(
        control: () -> T?,
        sample: Sample
    ): ExperimentObservation<T> =
        if (isDisabled(sample) && isReturnCandidate(sample)) {
            scrap(CONTROL)
        } else {
            execute(
                CONTROL,
                controlTimer,
                control,
                !isReturnCandidate(sample)
            )
        }

    private fun countExceptions(experimentObservation: ExperimentObservation<T>, exceptions: Counter) {
        if (experimentObservation.exception != null) {
            exceptions.increment()
        }
    }

    protected fun execute(
        type: ExperimentObservationType,
        timer: Timer,
        function: () -> T?,
        shouldThrow: Boolean
    ): ExperimentObservation<T> {
        val observation = ExperimentObservation<T>(type, timer)
        observation.time(function)
        val exception = observation.exception
        if (exception != null && shouldThrow) {
            throw exception
        }
        return observation
    }

    protected fun scrap(type: ExperimentObservationType): ExperimentObservation<T> {
        val experimentObservation: ExperimentObservation<T> = ExperimentObservation(type)
        experimentObservation.status = ExperimentObservationStatus.SCRAPPED
        return experimentObservation
    }

    open fun publish(result: Any, sample: Sample) {
        if (isPublishable(sample)) {
            eventBus.post(result)
        }
    }

    fun compare(control: ExperimentObservation<T>, candidate: ExperimentObservation<T>): ComparisonResult {
        countExceptions(candidate, candidateExceptionCount)
        return if (candidate.exception != null) {
            ComparisonResult("Candidate threw an exception.")
        } else {
            LOGGER.trace("Comparing\n{}\n{}", control.value, candidate.value)
            comparator.invoke(control.value, candidate.value)
        }.apply {
            LOGGER.debug("Compared...match={}", this.matches)
            totalCount.increment()
            if (!matches) {
                mismatchCount.increment()
            }
        }
    }

}
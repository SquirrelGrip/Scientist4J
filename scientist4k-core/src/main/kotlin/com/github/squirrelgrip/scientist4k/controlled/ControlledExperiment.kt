package com.github.squirrelgrip.scientist4k.controlled

import com.github.squirrelgrip.scientist4k.controlled.model.ControlledExperimentResult
import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

open class ControlledExperiment<T>(
    name: String,
    metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    sampleFactory: SampleFactory = SampleFactory(),
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT,
    sampleThreshold: Int = 100
) : AbstractExperiment<T>(
    name,
    metrics,
    comparator,
    sampleFactory,
    eventBus,
    experimentOptions,
    sampleThreshold
) {
    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val referenceTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "reference")

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlledExperiment::class.java)
    }

    private fun isReturnReference(sample: Sample) =
        sample.runOptions.contains(ExperimentOption.RETURN_REFERENCE) || experimentOptions.contains(ExperimentOption.RETURN_REFERENCE)

    open fun run(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
    ): T? {
        return if (isSync(sample)) {
            LOGGER.trace("Running sync")
            runSync(control, reference, candidate, sample)
        } else {
            LOGGER.trace("Running async")
            runAsync(control, reference, candidate, sample)
        }
    }

    open fun runSync(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create()
    ): T? {
        val controlExperimentObservation = executeControl(control, sample)
        val candidateObservation = executeCandidate(candidate, sample)
        val referenceObservation = executeReference(reference, sample)
        publishResult(
            controlExperimentObservation,
            referenceObservation,
            candidateObservation,
            sample
        ).handleComparisonMismatch()
        return controlExperimentObservation.value
    }

    open fun runAsync(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create()
    ) =
        runBlocking {
            val deferredControlObservation = GlobalScope.async {
                executeControl(control, sample)
            }
            val deferredCandidateObservation =
                GlobalScope.async {
                    executeCandidate(candidate, sample)
                }
            val deferredReferenceObservation =
                GlobalScope.async {
                    executeReference(reference, sample)
                }

            LOGGER.debug("Awaiting deferredControlObservation...")
            val controlObservation = deferredControlObservation.await()
            LOGGER.debug("deferredControlObservation is {}", controlObservation)
            val deferred = GlobalScope.async {
                publishAsync(controlObservation, deferredReferenceObservation, deferredCandidateObservation, sample)
            }
            if (isRaiseOnMismatch(sample)) {
                deferred.await().handleComparisonMismatch()
            }
            controlObservation.value
        }

    protected fun executeReference(
        reference: () -> T?,
        sample: Sample
    ): ExperimentObservation<T> =
        if (isDisabled(sample)) {
            scrap("reference")
        } else {
            execute("reference", referenceTimer, reference, isReturnReference(sample))
        }

    private suspend fun publishAsync(
        controlExperimentObservation: ExperimentObservation<T>,
        deferredReferenceExperimentObservation: Deferred<ExperimentObservation<T>>,
        deferredCandidateExperimentObservation: Deferred<ExperimentObservation<T>>,
        sample: Sample
    ): ControlledExperimentResult<T> {
        LOGGER.debug("Awaiting candidateObservation...")
        val candidateObservation = deferredCandidateExperimentObservation.await()
        LOGGER.debug("candidateObservation is {}", candidateObservation)
        LOGGER.debug("Awaiting referenceObservation...")
        val referenceObservation = deferredReferenceExperimentObservation.await()
        LOGGER.debug("referenceObservation is {}", referenceObservation)
        return publishResult(controlExperimentObservation, referenceObservation, candidateObservation, sample)
    }

    private fun publishResult(
        controlExperimentObservation: ExperimentObservation<T>,
        referenceExperimentObservation: ExperimentObservation<T>,
        candidateExperimentObservation: ExperimentObservation<T>,
        sample: Sample
    ): ControlledExperimentResult<T> {
        LOGGER.info("Creating Result...")
        val result = ControlledExperimentResult(
            this,
            controlExperimentObservation,
            referenceExperimentObservation,
            candidateExperimentObservation,
            sample
        )
        LOGGER.info("Created Result")
        publish(result, sample)
        return result
    }

}
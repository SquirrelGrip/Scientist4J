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
    experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
) : AbstractExperiment<T>(
    name,
    metrics,
    comparator,
    sampleFactory,
    eventBus,
    experimentOptions
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

    private fun isReturnReference(runOptions: EnumSet<ExperimentOption>) =
        runOptions.contains(ExperimentOption.RETURN_REFERENCE) || experimentOptions.contains(ExperimentOption.RETURN_REFERENCE)

    open fun run(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): T? {
        return if (isSync(runOptions)) {
            LOGGER.trace("Running sync")
            runSync(control, reference, candidate, sample, runOptions)
        } else {
            LOGGER.trace("Running async")
            runAsync(control, reference, candidate, sample, runOptions)
        }
    }

    open fun runSync(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): T? {
        val controlExperimentObservation = executeControl(control, runOptions)
        val candidateObservation = executeCandidate(candidate, runOptions)
        val referenceObservation = executeReference(reference, runOptions)
        publishResult(
            controlExperimentObservation,
            referenceObservation,
            candidateObservation,
            sample,
            runOptions
        ).handleComparisonMismatch()
        return controlExperimentObservation.value
    }

    open fun runAsync(
        control: () -> T?,
        reference: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ) =
        runBlocking {
            val deferredControlObservation = GlobalScope.async {
                executeControl(control, runOptions)
            }
            val deferredCandidateObservation =
                GlobalScope.async {
                    executeCandidate(candidate, runOptions)
                }
            val deferredReferenceObservation =
                GlobalScope.async {
                    executeReference(reference, runOptions)
                }


            LOGGER.debug("Awaiting deferredControlObservation...")
            val controlObservation = deferredControlObservation.await()
            LOGGER.debug("deferredControlObservation is {}", controlObservation)
            val deferred = GlobalScope.async {
                publishAsync(controlObservation, deferredReferenceObservation, deferredCandidateObservation, sample, runOptions)
            }
            if (isRaiseOnMismatch(runOptions)) {
                deferred.await().handleComparisonMismatch()
            }
            controlObservation.value
        }

    protected fun executeReference(
        reference: () -> T?,
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): ExperimentObservation<T> =
        if (experimentOptions.contains(ExperimentOption.DISABLED)) {
            scrap("reference")
        } else {
            execute("reference", referenceTimer, reference, isReturnReference(runOptions))
        }

    private suspend fun publishAsync(
        controlExperimentObservation: ExperimentObservation<T>,
        deferredReferenceExperimentObservation: Deferred<ExperimentObservation<T>>,
        deferredCandidateExperimentObservation: Deferred<ExperimentObservation<T>>,
        sample: Sample,
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): ControlledExperimentResult<T> {
        LOGGER.debug("Awaiting candidateObservation...")
        val candidateObservation = deferredCandidateExperimentObservation.await()
        LOGGER.debug("candidateObservation is {}", candidateObservation)
        LOGGER.debug("Awaiting referenceObservation...")
        val referenceObservation = deferredReferenceExperimentObservation.await()
        LOGGER.debug("referenceObservation is {}", referenceObservation)
        return publishResult(controlExperimentObservation, referenceObservation, candidateObservation, sample, runOptions)
    }

    private fun publishResult(
        controlExperimentObservation: ExperimentObservation<T>,
        referenceExperimentObservation: ExperimentObservation<T>,
        candidateExperimentObservation: ExperimentObservation<T>,
        sample: Sample,
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
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
        publish(result, runOptions)
        return result
    }

}
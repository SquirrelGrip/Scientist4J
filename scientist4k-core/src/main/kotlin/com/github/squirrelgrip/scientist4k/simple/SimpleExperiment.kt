package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

open class SimpleExperiment<T>(
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
    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SimpleExperiment::class.java)
    }

    open fun run(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): T? {
        sample.addNote("experiment", name)
        return if (isSync(runOptions)) {
            LOGGER.trace("Running sync")
            runSync(control, candidate, sample, runOptions)
        } else {
            LOGGER.trace("Running async")
            runAsync(control, candidate, sample, runOptions)
        }
    }

    open fun runSync(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ): T? {
        val controlObservation: ExperimentObservation<T> = executeControl(control)
        val candidateObservation: ExperimentObservation<T> = executeCandidate(candidate)
        publishResult(controlObservation, candidateObservation, sample, runOptions).handleComparisonMismatch()
        return if (isReturnCandidate(runOptions)) {
            candidateObservation.value
        } else {
            controlObservation.value
        }
    }


    open fun runAsync(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create(),
        runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    ) =
        runBlocking {
            val deferredControlObservation = GlobalScope.async {
                executeControl(control, runOptions)
            }
            val deferredCandidateObservation = GlobalScope.async {
                executeCandidate(candidate, runOptions)
            }

            val deferred = GlobalScope.async {
                publishAsync(deferredControlObservation, deferredCandidateObservation, sample, runOptions)
            }
            if (isRaiseOnMismatch(runOptions)) {
                deferred.await().handleComparisonMismatch()
            }
            if (isReturnCandidate(runOptions)) {
                deferredCandidateObservation.await().value
            } else {
                deferredControlObservation.await().value
            }
        }

    private suspend fun publishAsync(
        deferredControlExperimentObservation: Deferred<ExperimentObservation<T>>,
        deferredCandidateExperimentObservation: Deferred<ExperimentObservation<T>>,
        sample: Sample,
        runOptions: EnumSet<ExperimentOption>
    ): SimpleExperimentResult<T> {
        val controlObservation = deferredControlExperimentObservation.await()
        LOGGER.trace("controlObservation is {}", controlObservation)
        val candidateObservation = deferredCandidateExperimentObservation.await()
        LOGGER.trace("candidateObservation is {}", candidateObservation)
        return publishResult(controlObservation, candidateObservation, sample, runOptions)
    }

    private fun publishResult(
        controlExperimentObservation: ExperimentObservation<T>,
        candidateExperimentObservation: ExperimentObservation<T>,
        sample: Sample,
        runOptions: EnumSet<ExperimentOption>
    ): SimpleExperimentResult<T> {
        LOGGER.trace("Creating Result...")
        val result = SimpleExperimentResult(this, controlExperimentObservation, candidateExperimentObservation, sample)
        LOGGER.trace("Publishing Result")
        publish(result, runOptions)
        return result
    }

}
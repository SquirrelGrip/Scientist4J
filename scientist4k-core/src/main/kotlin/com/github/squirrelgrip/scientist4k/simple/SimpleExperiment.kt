package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag
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
    experimentFlags: EnumSet<ExperimentFlag> = ExperimentFlag.DEFAULT
) : AbstractExperiment<T>(
    name,
    metrics,
    comparator,
    sampleFactory,
    eventBus,
    experimentFlags
) {
    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SimpleExperiment::class.java)
    }

    open fun run(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        sample.addNote("experiment", name)
        return if (experimentFlags.contains(ExperimentFlag.SYNC)) {
            LOGGER.trace("Running sync")
            runSync(control, candidate, sample)
        } else {
            LOGGER.trace("Running async")
            runAsync(control, candidate, sample)
        }
    }

    open fun runSync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        val controlObservation: ExperimentObservation<T> = executeControl(control)
        val candidateObservation: ExperimentObservation<T> = executeCandidate(candidate)
        publishResult(controlObservation, candidateObservation, sample).handleComparisonMismatch()
        return if (experimentFlags.contains(ExperimentFlag.RETURN_CANDIDATE)) {
            candidateObservation.value
        } else {
            controlObservation.value
        }
    }

    open fun runAsync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()) =
        runBlocking {
            val deferredControlObservation = GlobalScope.async {
                executeControl(control)
            }
            val deferredCandidateObservation = GlobalScope.async {
                executeCandidate(candidate)
            }

            val deferred = GlobalScope.async {
                publishAsync(deferredControlObservation, deferredCandidateObservation, sample)
            }
            if (experimentFlags.contains(ExperimentFlag.RAISE_ON_MISMATCH)) {
                deferred.await().handleComparisonMismatch()
            }
            if (experimentFlags.contains(ExperimentFlag.RETURN_CANDIDATE)) {
                deferredCandidateObservation.await().value
            } else {
                deferredControlObservation.await().value
            }
        }

    private suspend fun publishAsync(
        deferredControlExperimentObservation: Deferred<ExperimentObservation<T>>,
        deferredCandidateExperimentObservation: Deferred<ExperimentObservation<T>>,
        sample: Sample
    ): SimpleExperimentResult<T> {
        val controlObservation = deferredControlExperimentObservation.await()
        LOGGER.trace("controlObservation is {}", controlObservation)
        val candidateObservation = deferredCandidateExperimentObservation.await()
        LOGGER.trace("candidateObservation is {}", candidateObservation)
        return publishResult(controlObservation, candidateObservation, sample)
    }

    private fun publishResult(
        controlExperimentObservation: ExperimentObservation<T>,
        candidateExperimentObservation: ExperimentObservation<T>,
        sample: Sample
    ): SimpleExperimentResult<T> {
        LOGGER.trace("Creating Result...")
        val result = SimpleExperimentResult(this, controlExperimentObservation, candidateExperimentObservation, sample)
        LOGGER.trace("Publishing Result")
        publish(result)
        return result
    }

}
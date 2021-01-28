package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class SimpleExperiment<T>(
    experimentConfiguration: ExperimentConfiguration,
    comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    eventBus: EventBus = DEFAULT_EVENT_BUS
) : AbstractExperiment<T>(
    experimentConfiguration,
    comparator,
    eventBus
) {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SimpleExperiment::class.java)
    }

    open fun run(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create()
    ): T? {
        return if (isSync(sample)) {
            LOGGER.trace("Running sync")
            runSync(control, candidate, sample)
        } else {
            LOGGER.trace("Running async")
            runAsync(control, candidate, sample)
        }
    }

    open fun runSync(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create()
    ): T? {
        val controlObservation: ExperimentObservation<T> = executeControl(control, sample)
        val candidateObservation: ExperimentObservation<T> = executeCandidate(candidate, sample)
        publishResult(controlObservation, candidateObservation, sample).handleComparisonMismatch()
        return if (isReturnCandidate(sample)) {
            candidateObservation.value
        } else {
            controlObservation.value
        }
    }

    open fun runAsync(
        control: () -> T?,
        candidate: () -> T?,
        sample: Sample = sampleFactory.create()
    ) =
        runBlocking {
            val deferredControlObservation = GlobalScope.async {
                executeControl(control, sample)
            }
            val deferredCandidateObservation = GlobalScope.async {
                executeCandidate(candidate, sample)
            }

            val deferred = GlobalScope.async {
                publishAsync(deferredControlObservation, deferredCandidateObservation, sample)
            }
            if (isRaiseOnMismatch(sample)) {
                deferred.await().handleComparisonMismatch()
            }
            if (isReturnCandidate(sample)) {
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
        sample.addNote("experiment", name)
        LOGGER.trace("Creating Result...")
        val result = SimpleExperimentResult(this, controlExperimentObservation, candidateExperimentObservation, sample)
        LOGGER.trace("Publishing Result")
        publish(result, sample)
        return result
    }

}
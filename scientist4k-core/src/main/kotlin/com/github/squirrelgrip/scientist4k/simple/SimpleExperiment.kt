package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class SimpleExperiment<T>(
        name: String,
        raiseOnMismatch: Boolean = false,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        eventBus: EventBus = DEFAULT_EVENT_BUS,
        enabled: Boolean = true,
        async: Boolean = true
) : AbstractExperiment<T>(
        name,
        raiseOnMismatch,
        metrics,
        comparator,
        sampleFactory,
        eventBus,
        enabled,
        async
) {
    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)
    constructor(name: String, metrics: MetricsProvider<*>) : this(name, false, metrics)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SimpleExperiment::class.java)
    }

    open fun run(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        sample.addNote("experiment", name)
        return if (isAsync) {
            LOGGER.trace("Running async")
            runAsync(control, candidate, sample)
        } else {
            LOGGER.trace("Running sync")
            runSync(control, candidate, sample)
        }
    }

    open fun runSync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        val controlExperimentObservation: ExperimentObservation<T> = executeControl(control)
        val candidateExperimentObservation: ExperimentObservation<T> = executeCandidate(candidate)
        publishResult(controlExperimentObservation, candidateExperimentObservation, sample).handleComparisonMismatch()
        return controlExperimentObservation.value
    }

    open fun runAsync(control: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()) =
            runBlocking {
                val deferredControlObservation = GlobalScope.async {
                    executeControl(control)
                }
                val deferredCandidateObservation =
                        GlobalScope.async {
                            executeCandidate(candidate)
                        }

                LOGGER.trace("Awaiting deferredControlObservation...")
                val controlObservation = deferredControlObservation.await()
                LOGGER.trace("deferredControlObservation is {}", controlObservation)
                val deferred = GlobalScope.async {
                    publishAsync(controlObservation, deferredCandidateObservation, sample)
                }
                if (raiseOnMismatch) {
                    deferred.await().handleComparisonMismatch()
                }
                controlObservation.value
            }

    private suspend fun publishAsync(controlExperimentObservation: ExperimentObservation<T>, deferredCandidateExperimentObservation: Deferred<ExperimentObservation<T>>, sample: Sample): SimpleExperimentResult<T> {
        LOGGER.trace("Awaiting candidateObservation...")
        val candidateObservation = deferredCandidateExperimentObservation.await()
        LOGGER.trace("candidateObservation is {}", candidateObservation)
        return publishResult(controlExperimentObservation, candidateObservation, sample)
    }

    private fun publishResult(controlExperimentObservation: ExperimentObservation<T>, candidateExperimentObservation: ExperimentObservation<T>, sample: Sample): SimpleExperimentResult<T> {
        LOGGER.trace("Creating Result...")
        val result = SimpleExperimentResult(this, controlExperimentObservation, candidateExperimentObservation, sample)
        LOGGER.trace("Publishing Result")
        publish(result)
        return result
    }

}
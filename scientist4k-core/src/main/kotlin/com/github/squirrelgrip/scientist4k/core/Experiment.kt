package com.github.squirrelgrip.scientist4k.core

import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.Observation
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

open class Experiment<T>(
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
        private val LOGGER: Logger = LoggerFactory.getLogger(Experiment::class.java)
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
        val controlObservation: Observation<T> = executeControl(control)
        val candidateObservation: Observation<T> = executeCandidate(candidate)
        publishResult(controlObservation, candidateObservation, sample).handleComparisonMismatch()
        return controlObservation.value
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

    private suspend fun publishAsync(controlObservation: Observation<T>, deferredCandidateObservation: Deferred<Observation<T>>, sample: Sample): ExperimentResult<T> {
        LOGGER.trace("Awaiting candidateObservation...")
        val candidateObservation = deferredCandidateObservation.await()
        LOGGER.trace("candidateObservation is {}", candidateObservation)
        return publishResult(controlObservation, candidateObservation, sample)
    }

    private fun publishResult(controlObservation: Observation<T>, candidateObservation: Observation<T>, sample: Sample): ExperimentResult<T> {
        LOGGER.trace("Creating Result...")
        val result = ExperimentResult(this, controlObservation, candidateObservation, sample)
        LOGGER.trace("Publishing Result")
        publish(result)
        return result
    }

}
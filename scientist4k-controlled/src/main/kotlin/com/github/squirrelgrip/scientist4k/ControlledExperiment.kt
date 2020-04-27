package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class ControlledExperiment<T>(
        name: String,
        raiseOnMismatch: Boolean = false,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        sampleFactory: SampleFactory = SampleFactory()
): AbstractExperiment<T>(
        name,
        raiseOnMismatch,
        metrics,
        context,
        comparator,
        sampleFactory
) {
    /**
     * Note that if `raiseOnMismatch` is true, [.runAsync] will block waiting for
     * the candidate function to complete before it can raise any resulting errors.
     * In situations where the candidate function may be significantly slower than the control,
     * it is *not* recommended to raise on mismatch.
     */
    private val publishers = mutableListOf<ControlledPublisher<T>>()
    private val referenceTimer: Timer = metrics.timer(NAMESPACE_PREFIX, name, "reference")

    constructor(metrics: MetricsProvider<*>) : this("Experiment", metrics)
    constructor(name: String, metrics: MetricsProvider<*>) : this(name, false, metrics)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlledExperiment::class.java)
    }

    fun addPublisher(publisher: ControlledPublisher<T>) {
        publishers.add(publisher)
    }

    fun removePublisher(publisher: ControlledPublisher<T>) {
        publishers.remove(publisher)
    }

    open fun run(control: () -> T?, reference: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        return if (isAsync) {
            LOGGER.trace("Running async")
            runAsync(control, reference, candidate, sample)
        } else {
            LOGGER.trace("Running sync")
            runSync(control, reference, candidate, sample)
        }
    }

    open fun runSync(control: () -> T?, reference: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        val controlObservation: Observation<T> = executeControl(control)
        val candidateObservation = if (runIf() && enabled()) {
            executeCandidate(candidate)
        } else {
            scrapCandidate()
        }
        val referenceObservation = if (runIf() && enabled()) {
            executeReference(reference)
        } else {
            scrapReference()
        }
        publishResult(controlObservation, referenceObservation, candidateObservation, sample).handleComparisonMismatch()
        return controlObservation.value
    }

    open fun runAsync(control: () -> T?, reference: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()) =
            runBlocking {
                val deferredControlObservation = GlobalScope.async {
                    executeControl(control)
                }
                val deferredCandidateObservation =
                        GlobalScope.async {
                            if (runIf() && enabled()) {
                                executeCandidate(candidate)
                            } else {
                                scrapCandidate()
                            }
                        }
                val deferredReferenceObservation =
                        GlobalScope.async {
                            if (runIf() && enabled()) {
                                executeReference(reference)
                            } else {
                                scrapReference()
                            }
                        }


                LOGGER.debug("Awaiting deferredControlObservation...")
                val controlObservation = deferredControlObservation.await()
                LOGGER.debug("deferredControlObservation is {}", controlObservation)
                val deferred = GlobalScope.async {
                    publishAsync(controlObservation, deferredReferenceObservation, deferredCandidateObservation, sample)
                }
                if (raiseOnMismatch) {
                    deferred.await().handleComparisonMismatch()
                }
                controlObservation.value
            }

    private suspend fun publishAsync(controlObservation: Observation<T>, deferredReferenceObservation: Deferred<Observation<T>>, deferredCandidateObservation: Deferred<Observation<T>>, sample: Sample): ControlledResult<T> {
        LOGGER.debug("Awaiting candidateObservation...")
        val candidateObservation = deferredCandidateObservation.await()
        LOGGER.debug("candidateObservation is {}", candidateObservation)
        LOGGER.debug("Awaiting referenceObservation...")
        val referenceObservation = deferredReferenceObservation.await()
        LOGGER.debug("referenceObservation is {}", referenceObservation)
        return publishResult(controlObservation, referenceObservation, candidateObservation, sample)
    }

    private fun publishResult(controlObservation: Observation<T>, referenceObservation: Observation<T>, candidateObservation: Observation<T>, sample: Sample): ControlledResult<T> {
        LOGGER.info("Creating Result...")
        val result = ControlledResult(this, controlObservation, referenceObservation, candidateObservation, context, sample)
        LOGGER.info("Created Result")
        publish(result)
        return result
    }

    private fun scrapReference(): Observation<T> {
        return scrap("reference")
    }

    private fun executeReference(reference: () -> T?) =
            execute("reference", referenceTimer, reference, false)

    open fun publish(result: ControlledResult<T>) {
        publishers.forEach {
            LOGGER.debug("Publishing to {}...", it)
            it.publish(result)
            LOGGER.debug("Published to {}", it)
        }
        result.sample.published.set(true)
    }

}
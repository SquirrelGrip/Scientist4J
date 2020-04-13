package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory

open class ControlledExperiment<T>(
        name: String,
        raiseOnMismatch: Boolean,
        metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        sampleFactory: SampleFactory = SampleFactory()
) : Experiment<T>(
        name,
        raiseOnMismatch,
        metricsProvider,
        context,
        comparator,
        sampleFactory
) {
    constructor(metricsProvider: MetricsProvider<*>) : this("ControlledExperiment", metricsProvider)
    constructor(name: String, metricsProvider: MetricsProvider<*>) : this(name, false, metricsProvider)
    constructor(name: String, context: Map<String, Any>, metricsProvider: MetricsProvider<*>) : this(name, false, metricsProvider, context)
    constructor(name: String, raiseOnMismatch: Boolean, metricsProvider: MetricsProvider<*>) : this(name, raiseOnMismatch, metricsProvider, mapOf<String, Any>())

    private val controlExperiment = object : Experiment<T>("$name-control", false, metricsProvider, context, comparator) {
        override fun publish(result: Result<T>) {
            processResult(result, true)
        }
    }

    fun run(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        return if (isAsync) {
            runAsync(controlPrimary, controlSecondary, candidate, sample)
        } else {
            runSync(controlPrimary, controlSecondary, candidate, sample)
        }
    }

    override fun run(control: () -> T?, candidate: () -> T?, sample: Sample): T? {
        return run(control, control, candidate, sample)
    }

    fun runSync(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        return super.runSync({ controlExperiment.runSync(controlPrimary, controlSecondary, sample) }, candidate, sample)
    }

    override fun runSync(control: () -> T?, candidate: () -> T?, sample: Sample): T? {
        return runSync(control, control, candidate, sample)
    }

    fun runAsync(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, sample: Sample = sampleFactory.create()): T? {
        return super.runAsync({ controlExperiment.runAsync(controlPrimary, controlSecondary, sample) }, candidate, sample)
    }

    override fun runAsync(control: () -> T?, candidate: () -> T?, sample: Sample): T? {
        return runAsync(control, control, candidate, sample)
    }

    override fun publish(result: Result<T>) {
        processResult(result, false)
    }

    private val matchingMap = mutableMapOf<String, Result<T>>()

    private fun processResult(result: Result<T>, control: Boolean) {
        synchronized(matchingMap) {
            val matchingResult = retrieveOrStoreResult(result)
            if (matchingResult != null) {
                if (control) {
                    publish(ControlledResult(result.sample, result, matchingResult))
                } else {
                    publish(ControlledResult(result.sample, matchingResult, result))
                }
            }
        }
    }

    private fun retrieveOrStoreResult(result: Result<T>): Result<T>? {
        val matchingResult = matchingMap[result.sample.id]
        if (matchingResult == null) {
            matchingMap[result.sample.id] = result
        } else {
            matchingMap.remove(result.sample.id)
        }
        return matchingResult
    }

    open fun publish(result: ControlledResult<T>) {
        println(result.controlResult.match)
        println(result.candidateResult.match)
        println(result.match)
    }

}
package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import java.util.function.BiFunction

open class ControlledExperiment<T>(
        name: String,
        context: Map<String, Any> = emptyMap(),
        raiseOnMismatch: Boolean,
        metricsProvider: MetricsProvider<*> = DropwizardMetricsProvider(),
        comparator: BiFunction<T?, T?, Boolean> = BiFunction { a: T?, b: T? -> a == b }
) : Experiment<T>(
        name,
        context,
        raiseOnMismatch,
        metricsProvider,
        comparator
) {
    constructor(metricsProvider: MetricsProvider<*>) : this("AdvancedExperiment", metricsProvider)
    constructor(name: String, metricsProvider: MetricsProvider<*>) : this(name, false, metricsProvider)
    constructor(name: String, context: Map<String, Any>, metricsProvider: MetricsProvider<*>) : this(name, context, false, metricsProvider)
    constructor(name: String, raiseOnMismatch: Boolean, metricsProvider: MetricsProvider<*>) : this(name, mapOf<String, Any>(), raiseOnMismatch, metricsProvider)

    private val controlExperiment = object : Experiment<T>("$name-control", context, false, metricsProvider, comparator) {
        override fun publish(result: Result<T>) {
            processResult(result, true)
        }
    }

    fun run(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, note: Note = Note()): T? {
        return if (isAsync) {
            runAsync(controlPrimary, controlSecondary, candidate, note)
        } else {
            runSync(controlPrimary, controlSecondary, candidate, note)
        }
    }

    override fun run(control: () -> T?, candidate: () -> T?, note: Note): T? {
        return run(control, control, candidate, note)
    }

    fun runSync(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, note: Note = Note()): T? {
        return super.runSync({ controlExperiment.runSync(controlPrimary, controlSecondary, note) }, candidate, note)
    }

    override fun runSync(control: () -> T?, candidate: () -> T?, note: Note): T? {
        return runSync(control, control, candidate, note)
    }

    fun runAsync(controlPrimary: () -> T?, controlSecondary: () -> T?, candidate: () -> T?, note: Note = Note()): T? {
        return super.runAsync({ controlExperiment.runAsync(controlPrimary, controlSecondary, note) }, candidate, note)
    }

    override fun runAsync(control: () -> T?, candidate: () -> T?, note: Note): T? {
        return runAsync(control, control, candidate, note)
    }

    override fun publish(result: Result<T>) {
        processResult(result, false)
    }

    private val matchingMap = mutableMapOf<Long, Result<T>>()

    private fun processResult(result: Result<T>, control: Boolean) {
        synchronized(matchingMap) {
            val matchingResult = retrieveOrStoreResult(result)
            if (matchingResult != null) {
                if (control) {
                    publish(ControlledResult(result.note, result, matchingResult))
                } else {
                    publish(ControlledResult(result.note, matchingResult, result))
                }
            }
        }
    }

    private fun retrieveOrStoreResult(result: Result<T>): Result<T>? {
        val matchingResult = matchingMap[result.note.sampleId]
        if (matchingResult == null) {
            matchingMap[result.note.sampleId] = result
        } else {
            matchingMap.remove(result.note.sampleId)
        }
        return matchingResult
    }

    open fun publish(result: ControlledResult<T>) {
        println(result.controlResult.match)
        println(result.candidateResult.match)
        println(result.match)
    }

}
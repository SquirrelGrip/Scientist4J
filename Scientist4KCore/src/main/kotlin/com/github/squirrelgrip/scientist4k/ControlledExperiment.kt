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
            println("control experiment publish called")
        }
    }

    fun run(controlA: () -> T?, controlB: () -> T?, candidate: () -> T?): T? {
        return if (isAsync) {
            runAsync(controlA, controlB, candidate)
        } else {
            runSync(controlA, controlB, candidate)
        }
    }

    fun runSync(controlA: () -> T?, controlB: () -> T?, candidate: () -> T?): T? {
        return super.runSync({ controlExperiment.runSync(controlA, controlB) }, candidate)
    }

    override fun runSync(control: () -> T?, candidate: () -> T?): T? {
        return runSync(control, control, candidate)
    }

    fun runAsync(controlA: () -> T?, controlB: () -> T?, candidate: () -> T?): T? {
        return super.runAsync({ controlExperiment.runAsync(controlA, controlB) }, candidate)
    }

    override fun runAsync(control: () -> T?, candidate: () -> T?): T? {
        return runAsync(control, control, candidate)
    }

    override fun publish(result: Result<T>) {
        println("candidate experiment publish called")
    }

}
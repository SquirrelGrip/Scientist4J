package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import java.util.function.BiFunction

class ExperimentBuilder<T> {
    private var name: String = "Test"
    private var metricsProvider: MetricsProvider<*> = DropwizardMetricsProvider()
    private var comparator: BiFunction<T?, T?, Boolean> = BiFunction { a: T?, b: T? -> a == b }
    private val context: Map<String, Any> = mutableMapOf()

    fun withName(name: String): ExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ExperimentBuilder<T> {
        this.metricsProvider = metricsProvider
        return this
    }

    fun withComparator(comparator: BiFunction<T?, T?, Boolean>): ExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun build(): Experiment<T> {
        return Experiment(name, context, false, metricsProvider, comparator)
    }

}
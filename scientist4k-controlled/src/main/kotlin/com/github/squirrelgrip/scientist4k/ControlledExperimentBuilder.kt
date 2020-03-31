package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator

class ControlledExperimentBuilder<T> {
    private var name: String = "Test"
    private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var comparator: ExperimentComparator<T> = DefaultExperimentComparator()
    private var raiseOnMismatch: Boolean = false

    fun withName(name: String): ControlledExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ControlledExperimentBuilder<T> {
        this.metricsProvider = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T>): ControlledExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ControlledExperimentBuilder<T> {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun build(): ControlledExperiment<T> {
        return ControlledExperiment(name, raiseOnMismatch, metricsProvider, mutableMapOf(), comparator)
    }

}
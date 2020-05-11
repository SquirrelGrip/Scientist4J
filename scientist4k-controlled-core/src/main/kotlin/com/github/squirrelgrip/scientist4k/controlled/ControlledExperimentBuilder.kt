package com.github.squirrelgrip.scientist4k.controlled

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.AbstractExperiment.Companion.DEFAULT_EVENT_BUS
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class ControlledExperimentBuilder<T>(
        private var name: String = "Test",
        private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        private var raiseOnMismatch: Boolean = false,
        private var sampleFactory: SampleFactory = SampleFactory(),
        private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        private var eventBus: EventBus = DEFAULT_EVENT_BUS
) {
    constructor(experimentConfiguration: ExperimentConfiguration): this(
            experimentConfiguration.name,
            experimentConfiguration.metrics,
            experimentConfiguration.raiseOnMismatch,
            experimentConfiguration.sampleFactory
    )

    fun withName(name: String): ControlledExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ControlledExperimentBuilder<T> {
        this.metricsProvider = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T?>): ControlledExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ControlledExperimentBuilder<T> {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): ControlledExperimentBuilder<T> {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledExperimentBuilder<T> {
        this.eventBus = eventBus
        return this
    }

    fun build(): ControlledExperiment<T> {
        return ControlledExperiment(name, raiseOnMismatch, metricsProvider, comparator, sampleFactory, eventBus)
    }

}
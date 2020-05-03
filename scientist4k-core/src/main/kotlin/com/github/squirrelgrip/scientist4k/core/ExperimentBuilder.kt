package com.github.squirrelgrip.scientist4k.core

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class ExperimentBuilder<T>(
        private var name: String = "Test",
        private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        private var raiseOnMismatch: Boolean = false,
        private var sampleFactory: SampleFactory = SampleFactory(),
        private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
        private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
) {
    constructor(experimentConfiguration: ExperimentConfiguration): this(
        experimentConfiguration.name,
        experimentConfiguration.metrics,
        experimentConfiguration.raiseOnMismatch,
        experimentConfiguration.sampleFactory
    )

    fun withName(name: String): ExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): ExperimentBuilder<T> {
        this.metricsProvider = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ExperimentBuilder<T> {
        this.metricsProvider = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T?>): ExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ExperimentBuilder<T> {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): ExperimentBuilder<T> {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withEventBus(eventBus: EventBus): ExperimentBuilder<T> {
        this.eventBus = eventBus
        return this
    }

    fun build(): Experiment<T> {
        return Experiment(name, raiseOnMismatch, metricsProvider, comparator, sampleFactory, eventBus)
    }

}
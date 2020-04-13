package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory

class ExperimentBuilder<T>(
    private var name: String = "Test",
    private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    private var raiseOnMismatch: Boolean = false,
    private var sampleFactory: SampleFactory = SampleFactory(),
    private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    private var context: Map<String, String> = emptyMap()
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

    fun withContext(context: Map<String, String>): ExperimentBuilder<T> {
        this.context = context
        return this
    }

    fun build(): Experiment<T> {
        return Experiment(name, raiseOnMismatch, metricsProvider, context, comparator, sampleFactory)
    }

}
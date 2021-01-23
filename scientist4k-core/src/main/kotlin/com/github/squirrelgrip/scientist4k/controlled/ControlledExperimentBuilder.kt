package com.github.squirrelgrip.scientist4k.controlled

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment.Companion.DEFAULT_EVENT_BUS
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import java.util.*

class ControlledExperimentBuilder<T>(
    private var name: String = "Test",
    private var metricsProvider: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    private var sampleFactory: SampleFactory = SampleFactory(),
    private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator(),
    private var eventBus: EventBus = DEFAULT_EVENT_BUS,
    private var experimentFlags: EnumSet<ExperimentFlag> = ExperimentFlag.DEFAULT
) {
    constructor(experimentConfiguration: ExperimentConfiguration): this(
        experimentConfiguration.name,
        experimentConfiguration.metrics,
        experimentConfiguration.sampleFactory,
        DefaultExperimentComparator(),
        DEFAULT_EVENT_BUS,
        experimentConfiguration.experimentFlags
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

    fun withExperimentFlags(vararg experimentFlag: ExperimentFlag): ControlledExperimentBuilder<T> {
        this.experimentFlags = EnumSet.copyOf(experimentFlag.asList())
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
        return ControlledExperiment(name, metricsProvider, comparator, sampleFactory, eventBus)
    }

}
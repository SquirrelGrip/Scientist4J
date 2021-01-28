package com.github.squirrelgrip.scientist4k.controlled

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment.Companion.DEFAULT_EVENT_BUS
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.google.common.eventbus.EventBus
import java.util.*

class ControlledExperimentBuilder<T>() {

    private var name: String = "Test"
    private var metrics: Metrics = Metrics.DROPWIZARD
    private var samplePrefix: String = ""
    private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator()
    private var eventBus: EventBus = DEFAULT_EVENT_BUS
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var sampleThreshold: Int = 100

    constructor(experimentConfiguration: ExperimentConfiguration) : this() {
        name = experimentConfiguration.name
        metrics = experimentConfiguration.metrics
        samplePrefix = experimentConfiguration.samplePrefix
        experimentOptions = experimentConfiguration.experimentOptions
        sampleThreshold = experimentConfiguration.sampleThreshold
    }

    fun withName(name: String): ControlledExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetrics(metrics: Metrics): ControlledExperimentBuilder<T> {
        this.metrics = metrics
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T?>): ControlledExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withExperimentOptions(vararg experimentOption: ExperimentOption): ControlledExperimentBuilder<T> {
        this.experimentOptions = EnumSet.copyOf(experimentOption.asList())
        return this
    }

    fun withSamplePrefix(samplePrefix: String): ControlledExperimentBuilder<T> {
        this.samplePrefix = samplePrefix
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledExperimentBuilder<T> {
        this.eventBus = eventBus
        return this
    }

    fun withSampleThreshold(sampleThreshold: Int): ControlledExperimentBuilder<T> {
        this.sampleThreshold = sampleThreshold
        return this
    }

    fun build(): ControlledExperiment<T> {
        val experimentConfiguration =
            ExperimentConfiguration(name, metrics, samplePrefix, experimentOptions, sampleThreshold)
        return ControlledExperiment(experimentConfiguration, comparator, eventBus)
    }

}
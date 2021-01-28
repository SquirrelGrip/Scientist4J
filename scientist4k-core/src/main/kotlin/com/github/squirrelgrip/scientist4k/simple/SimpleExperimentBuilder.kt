package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.google.common.eventbus.EventBus
import java.util.*

class SimpleExperimentBuilder<T>() {
    private var name: String = "Test"
    private var metrics: Metrics = Metrics.DROPWIZARD
    private var samplePrefix: String = ""
    private var sampleThreshold: Int = 100
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var comparator: ExperimentComparator<T?> = DefaultExperimentComparator()

    constructor(experimentConfiguration: ExperimentConfiguration): this() {
        name = experimentConfiguration.name
        metrics= experimentConfiguration.metrics
        samplePrefix = experimentConfiguration.samplePrefix
        experimentOptions = experimentConfiguration.experimentOptions
        sampleThreshold = experimentConfiguration.sampleThreshold
    }

    fun withName(name: String): SimpleExperimentBuilder<T> {
        this.name = name
        return this
    }

    fun withMetrics(metrics: Metrics): SimpleExperimentBuilder<T> {
        this.metrics = metrics
        return this
    }

    fun withComparator(comparator: ExperimentComparator<T?>): SimpleExperimentBuilder<T> {
        this.comparator = comparator
        return this
    }

    fun withSamplePrefix(samplePrefix: String): SimpleExperimentBuilder<T> {
        this.samplePrefix = samplePrefix
        return this
    }

    fun withEventBus(eventBus: EventBus): SimpleExperimentBuilder<T> {
        this.eventBus = eventBus
        return this
    }

    fun withExperimentOptions(vararg experimentOption: ExperimentOption): SimpleExperimentBuilder<T> {
        this.experimentOptions = EnumSet.copyOf(experimentOption.toList())
        return this
    }

    fun withSampleThreshold(sampleThreshold: Int): SimpleExperimentBuilder<T> {
        this.sampleThreshold = sampleThreshold
        return this
    }

    fun build(): SimpleExperiment<T> {
        val experimentConfiguration = ExperimentConfiguration(name, metrics, samplePrefix, experimentOptions, sampleThreshold)
        return SimpleExperiment(
            experimentConfiguration,
            comparator,
            eventBus
        )
    }

}
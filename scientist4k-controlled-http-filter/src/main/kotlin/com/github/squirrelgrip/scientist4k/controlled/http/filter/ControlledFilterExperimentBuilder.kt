package com.github.squirrelgrip.scientist4k.controlled.http.filter

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment.Companion.DEFAULT_EVENT_BUS
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import java.util.*

class ControlledFilterExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var sampleFactory: SampleFactory = SampleFactory()
    private var eventBus: EventBus = DEFAULT_EVENT_BUS
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var detourConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null

    constructor(httpExperimentConfiguration: ControlledFilterExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
        experimentOptions = httpExperimentConfiguration.experiment.experimentOptions
        detourConfig = httpExperimentConfiguration.detour
        referenceConfig = httpExperimentConfiguration.reference
        mappings = httpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): ControlledFilterExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): ControlledFilterExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ControlledFilterExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): ControlledFilterExperimentBuilder {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withDetourConfig(detourConfiguration: EndPointConfiguration): ControlledFilterExperimentBuilder {
        this.detourConfig = detourConfiguration
        return this
    }

    fun withReferenceConfig(referenceConfiguration: EndPointConfiguration): ControlledFilterExperimentBuilder {
        this.referenceConfig = referenceConfiguration
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): ControlledFilterExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledFilterExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withExperimentOptions(vararg experimentOption: ExperimentOption): ControlledFilterExperimentBuilder {
        this.experimentOptions = EnumSet.copyOf(experimentOption.toList())
        return this
    }

    fun build(): ControlledFilterExperiment {
        if (detourConfig != null) {
            return ControlledFilterExperiment(name, metrics, sampleFactory, eventBus, experimentOptions, mappings, detourConfig!!, referenceConfig!!)
        }
        throw LaboratoryException("Detour configurations must be set")
    }

}
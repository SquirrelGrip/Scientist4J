package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class FilterExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var sampleFactory: SampleFactory = SampleFactory()
    private var detourConfig: EndPointConfiguration? = null
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var enabled: Boolean = true
    private var async: Boolean = true

    constructor(httpExperimentConfiguration: FilterExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
        detourConfig = httpExperimentConfiguration.detour
        mappings = httpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): FilterExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): FilterExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): FilterExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): FilterExperimentBuilder {
        this.sampleFactory = sampleFactory
        return this
    }

    fun withDetourConfig(detourConfiguration: EndPointConfiguration): FilterExperimentBuilder {
        this.detourConfig = detourConfiguration
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): FilterExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun withEventBus(eventBus: EventBus): FilterExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withEnabled(enabled: Boolean): FilterExperimentBuilder {
        this.enabled = enabled
        return this
    }

    fun withAsync(async: Boolean): FilterExperimentBuilder {
        this.async = async
        return this
    }

    fun build(): FilterSimpleExperiment {
        if (detourConfig != null) {
            return FilterSimpleExperiment(name, metrics, sampleFactory, eventBus, enabled, async, mappings, detourConfig!!)
        }
        throw LaboratoryException("Detour configurations must be set")
    }

}
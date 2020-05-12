package com.github.squirrelgrip.scientist4k.controlled.http.filter

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment.Companion.DEFAULT_EVENT_BUS
import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.comparator.DefaultExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus

class ControlledFilterExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var raiseOnMismatch: Boolean = false
    private var sampleFactory: SampleFactory = SampleFactory()
    private var comparator: ExperimentComparator<ExperimentResponse?> = DefaultExperimentResponseComparator()
    private var eventBus: EventBus = DEFAULT_EVENT_BUS
    private var enabled: Boolean = true
    private var async: Boolean = true
    private var detourConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null

    constructor(httpExperimentConfiguration: ControlledFilterExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        raiseOnMismatch = httpExperimentConfiguration.experiment.raiseOnMismatch
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
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

    fun withComparator(comparator: ExperimentComparator<ExperimentResponse?>): ControlledFilterExperimentBuilder {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ControlledFilterExperimentBuilder {
        this.raiseOnMismatch = raiseOnMismatch
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

    fun withEnabled(enabled: Boolean): ControlledFilterExperimentBuilder {
        this.enabled = enabled
        return this
    }

    fun withAsync(async: Boolean): ControlledFilterExperimentBuilder {
        this.async = async
        return this
    }

    fun build(): ControlledFilterExperiment {
        if (detourConfig != null) {
            return ControlledFilterExperiment(name, raiseOnMismatch, metrics, comparator, sampleFactory, eventBus, enabled, async, mappings, detourConfig!!, referenceConfig!!)
        }
        throw LaboratoryException("Detour configurations must be set")
    }

}
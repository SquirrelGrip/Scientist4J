package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.github.squirrelgrip.scientist4k.metrics.Metrics.DROPWIZARD
import com.google.common.eventbus.EventBus
import java.util.*

class FilterExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: Metrics = DROPWIZARD
    private var samplePrefix: String = ""
    private var detourConfig: EndPointConfiguration? = null
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var sampleThreshold: Int = 100

    constructor(httpExperimentConfiguration: FilterExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        samplePrefix = httpExperimentConfiguration.experiment.samplePrefix
        experimentOptions = httpExperimentConfiguration.experiment.experimentOptions
        detourConfig = httpExperimentConfiguration.detour
        mappings = httpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): FilterExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetrics(metrics: Metrics): FilterExperimentBuilder {
        this.metrics = metrics
        return this
    }

    fun withSamplePrefix(samplePrefix: String): FilterExperimentBuilder {
        this.samplePrefix = samplePrefix
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

    fun withExperimentFlags(vararg experimentOption: ExperimentOption): FilterExperimentBuilder {
        this.experimentOptions = EnumSet.copyOf(experimentOption.toList())
        return this
    }

    fun withSampleThreshold(sampleThreshold: Int): FilterExperimentBuilder {
        this.sampleThreshold = sampleThreshold
        return this
    }

    fun build(): FilterSimpleExperiment {
        if (detourConfig != null) {
            return FilterSimpleExperiment(
                ExperimentConfiguration(name, metrics, samplePrefix, experimentOptions, sampleThreshold),
                eventBus,
                mappings,
                detourConfig!!
            )
        }
        throw LaboratoryException("Detour configurations must be set")
    }

}
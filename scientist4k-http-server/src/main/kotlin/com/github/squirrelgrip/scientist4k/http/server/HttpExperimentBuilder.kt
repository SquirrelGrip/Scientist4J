package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.google.common.eventbus.EventBus
import java.util.*

class HttpExperimentBuilder() {
    private var mappings: MappingsConfiguration = MappingsConfiguration()
    private var name: String = "Test"
    private var metrics: Metrics = Metrics.DROPWIZARD
    private var samplePrefix: String = ""
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null
    private var sampleThreshold: Int = 100

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        samplePrefix = httpExperimentConfiguration.experiment.samplePrefix
        experimentOptions = httpExperimentConfiguration.experiment.experimentOptions
        sampleThreshold = httpExperimentConfiguration.experiment.sampleThreshold
        controlConfig = httpExperimentConfiguration.control
        candidateConfig = httpExperimentConfiguration.candidate
        mappings = httpExperimentConfiguration.mappings
    }

    fun withName(name: String): HttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetrics(metrics: Metrics): HttpExperimentBuilder {
        this.metrics = metrics
        return this
    }

    fun withSamplePrefix(samplePrefix: String): HttpExperimentBuilder {
        this.samplePrefix = samplePrefix
        return this
    }

    fun withControlConfig(controlConfiguration: EndPointConfiguration): HttpExperimentBuilder {
        this.controlConfig = controlConfiguration
        return this
    }

    fun withCandidateConfig(candidateConfiguration: EndPointConfiguration): HttpExperimentBuilder {
        this.candidateConfig = candidateConfiguration
        return this
    }

    fun withEventBus(eventBus: EventBus): HttpExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withSampleThreshold(sampleThreshold: Int): HttpExperimentBuilder {
        this.sampleThreshold = sampleThreshold
        return this
    }

    fun withMappings(mappings: MappingsConfiguration): HttpExperimentBuilder {
        this.mappings = mappings
        return this
    }

    fun withExperimentOptions(vararg experimentOption: ExperimentOption): HttpExperimentBuilder {
        this.experimentOptions = EnumSet.copyOf(experimentOption.asList())
        return this
    }

    fun build(): SimpleHttpExperiment {
        if (controlConfig != null && candidateConfig != null) {
            return SimpleHttpExperiment(
                ExperimentConfiguration(name, metrics, samplePrefix, experimentOptions, sampleThreshold),
                eventBus,
                controlConfig!!,
                mappings,
                candidateConfig!!
            )
        }
        throw LaboratoryException("Both control and candidate configurations must be set")
    }

}
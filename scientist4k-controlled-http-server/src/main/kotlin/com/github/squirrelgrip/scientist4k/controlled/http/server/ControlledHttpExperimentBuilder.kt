package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.google.common.eventbus.EventBus
import java.util.*

class ControlledHttpExperimentBuilder() {
    private var sampleThreshold: Int = 100
    private var mappings: MappingsConfiguration = MappingsConfiguration()
    private var name: String = "Test"
    private var metrics: Metrics = Metrics.DROPWIZARD
    private var samplePrefix: String = ""
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        samplePrefix = httpExperimentConfiguration.experiment.samplePrefix
        experimentOptions = httpExperimentConfiguration.experiment.experimentOptions
        controlConfig = httpExperimentConfiguration.control
        candidateConfig = httpExperimentConfiguration.candidate
        referenceConfig = httpExperimentConfiguration.reference
        mappings = httpExperimentConfiguration.mappings
    }

    fun withName(name: String): ControlledHttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetrics(metrics: Metrics): ControlledHttpExperimentBuilder {
        this.metrics = metrics
        return this
    }

    fun withSamplePrefix(samplePrefix: String): ControlledHttpExperimentBuilder {
        this.samplePrefix = samplePrefix
        return this
    }

    fun withControlConfig(controlConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.controlConfig = controlConfiguration
        return this
    }

    fun withCandidateConfig(candidateConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.candidateConfig = candidateConfiguration
        return this
    }

    fun withReferenceConfig(referenceConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.referenceConfig = referenceConfiguration
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledHttpExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withExperimentOptions(vararg experimentOption: ExperimentOption): ControlledHttpExperimentBuilder {
        this.experimentOptions = EnumSet.copyOf(experimentOption.toList())
        return this
    }

    fun withMappings(mappings: MappingsConfiguration): ControlledHttpExperimentBuilder {
        this.mappings = mappings
        return this
    }

    fun withSampleThreshold(sampleThreshold: Int): ControlledHttpExperimentBuilder {
        this.sampleThreshold = sampleThreshold
        return this
    }

    fun build(): ControlledHttpExperiment {
        if (controlConfig != null && referenceConfig != null && candidateConfig != null) {
            return ControlledHttpExperiment(
                ExperimentConfiguration(name, metrics, samplePrefix, experimentOptions, sampleThreshold),
                eventBus,
                mappings,
                controlConfig!!,
                referenceConfig!!,
                candidateConfig!!
            )
        }
        throw LaboratoryException("primaryControl, secondaryControl and candidate configurations must be set")
    }

}
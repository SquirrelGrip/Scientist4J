package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.exception.LaboratoryException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import java.util.*

class ControlledHttpExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var sampleFactory: SampleFactory = SampleFactory()
    private var eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS
    private var experimentFlags: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null


    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
        experimentFlags = httpExperimentConfiguration.experiment.experimentFlags
        controlConfig = httpExperimentConfiguration.control
        candidateConfig = httpExperimentConfiguration.candidate
        referenceConfig = httpExperimentConfiguration.reference
        mappings = httpExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }
    }

    fun withName(name: String): ControlledHttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): ControlledHttpExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): ControlledHttpExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): ControlledHttpExperimentBuilder {
        this.sampleFactory = sampleFactory
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

    fun withExperimentFlags(vararg experimentOption: ExperimentOption): ControlledHttpExperimentBuilder {
        this.experimentFlags = EnumSet.copyOf(experimentOption.toList())
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): ControlledHttpExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun build(): ControlledHttpExperiment {
        if (controlConfig != null && referenceConfig != null && candidateConfig != null) {
            return ControlledHttpExperiment(name, metrics, sampleFactory, eventBus, experimentFlags, mappings, controlConfig!!, referenceConfig!!, candidateConfig!!)
        }
        throw LaboratoryException("primaryControl, secondaryControl and candidate configurations must be set")
    }

}
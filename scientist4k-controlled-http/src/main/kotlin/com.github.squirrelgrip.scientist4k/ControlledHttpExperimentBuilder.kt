package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.comparator.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.configuration.ControlledHttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.exceptions.LaboratoryException
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import com.google.common.eventbus.EventBus

class ControlledHttpExperimentBuilder() {
    private var mappings: List<MappingConfiguration> = emptyList()
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var raiseOnMismatch: Boolean = false
    private var sampleFactory: SampleFactory = SampleFactory()
    private var comparator: ExperimentComparator<ExperimentResponse?> = ExperimentResponseComparator()
    private var context: Map<String, String> = emptyMap()
    private var eventBus: EventBus = EventBus()
    private var controlConfig: EndPointConfiguration? = null
    private var referenceConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null

    constructor(controlledHttpExperimentConfiguration: ControlledHttpExperimentConfiguration) : this() {
        name = controlledHttpExperimentConfiguration.experiment.name
        metrics = controlledHttpExperimentConfiguration.experiment.metrics
        raiseOnMismatch = controlledHttpExperimentConfiguration.experiment.raiseOnMismatch
        sampleFactory = controlledHttpExperimentConfiguration.experiment.sampleFactory
        controlConfig = controlledHttpExperimentConfiguration.control
        referenceConfig = controlledHttpExperimentConfiguration.reference
        candidateConfig = controlledHttpExperimentConfiguration.candidate
        mappings = controlledHttpExperimentConfiguration.mappings.map { (control, candidate) ->
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

    fun withComparator(comparator: ExperimentComparator<ExperimentResponse?>): ControlledHttpExperimentBuilder {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): ControlledHttpExperimentBuilder {
        this.raiseOnMismatch = raiseOnMismatch
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

    fun withReferenceConfig(referenceConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.referenceConfig = referenceConfiguration
        return this
    }

    fun withCandidateConfig(candidateConfiguration: EndPointConfiguration): ControlledHttpExperimentBuilder {
        this.candidateConfig = candidateConfiguration
        return this
    }

    fun withEventBus(eventBus: EventBus): ControlledHttpExperimentBuilder {
        this.eventBus = eventBus
        return this
    }

    fun withMappings(vararg mapping: MappingConfiguration): ControlledHttpExperimentBuilder {
        this.mappings = mapping.toList()
        return this
    }

    fun build(): ControlledHttpExperiment {
        if (controlConfig != null && referenceConfig != null && candidateConfig != null) {
            return ControlledHttpExperiment(name, raiseOnMismatch, metrics, context, comparator, sampleFactory, eventBus, mappings, controlConfig!!, referenceConfig!!, candidateConfig!!)
        }
        throw LaboratoryException("primaryControl, secondaryControl and candidate configurations must be set")
    }

}
package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.exceptions.LaboratoryException
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.model.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory

class HttpExperimentBuilder() {
    private var name: String = "Test"
    private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD")
    private var raiseOnMismatch: Boolean = false
    private var sampleFactory: SampleFactory = SampleFactory()
    private var comparator: ExperimentComparator<ExperimentResponse> = ExperimentResponseComparator()
    private var controlConfig: EndPointConfiguration? = null
    private var candidateConfig: EndPointConfiguration? = null

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this() {
        name = httpExperimentConfiguration.experiment.name
        metrics = httpExperimentConfiguration.experiment.metrics
        raiseOnMismatch = httpExperimentConfiguration.experiment.raiseOnMismatch
        sampleFactory = httpExperimentConfiguration.experiment.sampleFactory
        controlConfig = httpExperimentConfiguration.control
        candidateConfig = httpExperimentConfiguration.candidate
    }

    fun withName(name: String): HttpExperimentBuilder {
        this.name = name
        return this
    }

    fun withMetricsProvider(metricsProvider: String): HttpExperimentBuilder {
        this.metrics = MetricsProvider.build(metricsProvider)
        return this
    }

    fun withMetricsProvider(metricsProvider: MetricsProvider<*>): HttpExperimentBuilder {
        this.metrics = metricsProvider
        return this
    }

    fun withComparator(comparator: ExperimentComparator<ExperimentResponse>): HttpExperimentBuilder {
        this.comparator = comparator
        return this
    }

    fun withRaiseOnMismatch(raiseOnMismatch: Boolean): HttpExperimentBuilder {
        this.raiseOnMismatch = raiseOnMismatch
        return this
    }

    fun withSampleFactory(sampleFactory: SampleFactory): HttpExperimentBuilder {
        this.sampleFactory = sampleFactory
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

    fun build(): HttpExperiment {
        if (controlConfig != null && candidateConfig != null) {
            return HttpExperiment(name, raiseOnMismatch, metrics, mutableMapOf(), comparator, sampleFactory, controlConfig!!, candidateConfig!!)
        }
        throw LaboratoryException("Both control and candidate configurations must be set")
    }

}
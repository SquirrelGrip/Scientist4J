package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.HttpResponseComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import org.apache.http.HttpResponse

class HttpExperimentBuilder(
        private var name: String = "Test",
        private var metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        private var raiseOnMismatch: Boolean = false,
        private var sampleFactory: SampleFactory = SampleFactory(),
        private var comparator: ExperimentComparator<HttpResponse> = HttpResponseComparator(),
        private var controlUrl: String = "",
        private var candidateUrl: String = "",
        private var allowedMethods: List<String> = listOf("GET"),
        private var controlSslConfiguration: SslConfiguration? = null,
        private var candidateSslConfiguration: SslConfiguration? = null
) {
    constructor(httpExperimentConfiguration: HttpExperimentConfiguration) : this(
            name = httpExperimentConfiguration.experimentConfig.name,
            metrics = httpExperimentConfiguration.experimentConfig.metrics,
            raiseOnMismatch = httpExperimentConfiguration.experimentConfig.raiseOnMismatch,
            sampleFactory = httpExperimentConfiguration.experimentConfig.sampleFactory,
            controlUrl = httpExperimentConfiguration.controlUrl,
            candidateUrl = httpExperimentConfiguration.candidateUrl,
            allowedMethods = httpExperimentConfiguration.allowedMethods,
            controlSslConfiguration = httpExperimentConfiguration.controlSslConfiguration,
            candidateSslConfiguration = httpExperimentConfiguration.candidateSslConfiguration
    )

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

    fun withComparator(comparator: ExperimentComparator<HttpResponse>): HttpExperimentBuilder {
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

    fun withControlUrl(controlUrl: String): HttpExperimentBuilder {
        this.controlUrl = controlUrl
        return this
    }

    fun withCandidateUrl(candidateUrl: String): HttpExperimentBuilder {
        this.candidateUrl = candidateUrl
        return this
    }

    fun withAllowedMethods(allowedMethods: List<String>): HttpExperimentBuilder {
        this.allowedMethods = allowedMethods
        return this
    }

    fun withControlSslConfiguration(controlSslConfiguration: SslConfiguration): HttpExperimentBuilder {
        this.controlSslConfiguration = controlSslConfiguration
        return this
    }

    fun withCandidateSslConfiguration(candidateSslConfiguration: SslConfiguration): HttpExperimentBuilder {
        this.candidateSslConfiguration = candidateSslConfiguration
        return this
    }

    fun build(): HttpExperiment {
        return HttpExperiment(name, raiseOnMismatch, metrics, mutableMapOf(), comparator, sampleFactory, controlUrl, candidateUrl, allowedMethods, controlSslConfiguration, candidateSslConfiguration)
    }

}
package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.core.Experiment
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.processResponse
import com.github.squirrelgrip.scientist4k.http.core.comparator.DefaultExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        comparator: ExperimentComparator<ExperimentResponse?> = DefaultExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        eventBus: EventBus = DEFAULT_EVENT_BUS,
        mappings: List<MappingConfiguration> = emptyList(),
        enabled: Boolean = true,
        async: Boolean = true,
        controlConfig: EndPointConfiguration,
        private val candidateConfig: EndPointConfiguration
) : Experiment<ExperimentResponse>(
        name,
        raiseOnMismatch,
        metrics,
        comparator,
        sampleFactory,
        eventBus,
        enabled,
        async
) {
    private val controlRequestFactory = RequestFactory(controlConfig, HttpExperimentUtil.CONTROL_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, HttpExperimentUtil.CANDIDATE_COOKIE_STORE, mappings)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(HttpExperiment::class.java)
    }

    fun run(
            inboundRequest: HttpServletRequest,
            inboundResponse: HttpServletResponse,
            sample: Sample = sampleFactory.create()
    ) {
        val experimentRequest = HttpExperimentUtil.createRequest(inboundRequest, sample)
        val controlRequest = createControlRequest(experimentRequest)

        val controlResponse = if (candidateConfig.allowedMethods.contains("*") or candidateConfig.allowedMethods.contains(inboundRequest.method)) {
            val candidateRequest = createCandidateRequest(experimentRequest)
            run(controlRequest, candidateRequest, sample)
        } else {
            controlRequest.invoke()
        }
        processResponse(inboundResponse, controlResponse)
    }

    private fun createControlRequest(
            request: ExperimentRequest
    ): () -> ExperimentResponse {
        return controlRequestFactory.create(request)
    }

    private fun createCandidateRequest(
            request: ExperimentRequest
    ): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }

}



package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.core.AbstractSimpleHttpExperiment
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil.processResponse
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SimpleHttpExperiment(
    experimentConfiguration: ExperimentConfiguration,
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    controlConfig: EndPointConfiguration,
    private val mappings: MappingsConfiguration,
    private val candidateConfig: EndPointConfiguration
) : AbstractSimpleHttpExperiment(
    experimentConfiguration,
    eventBus,
) {
    private val controlRequestFactory = RequestFactory(controlConfig, HttpExperimentUtil.CONTROL_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, HttpExperimentUtil.CANDIDATE_COOKIE_STORE, mappings)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SimpleHttpExperiment::class.java)
    }

    fun run(
        inboundRequest: HttpServletRequest,
        inboundResponse: HttpServletResponse
    ) {
        val sample: Sample = sampleFactory.create(mappings.getRunOptions(inboundRequest))
        val experimentRequest = HttpExperimentUtil.createRequest(inboundRequest, sample)

        val controlRequest = createControlRequest(experimentRequest)
        val candidateRequest = createCandidateRequest(experimentRequest)

        val resultResponse =
            if (isMethodAllowed(inboundRequest)) {
                run(controlRequest, candidateRequest, sample)
            } else {
                if (isReturnCandidate(sample)) {
                    candidateRequest.invoke()
                } else {
                    controlRequest.invoke()
                }
            }
        processResponse(inboundResponse, resultResponse)
    }

    private fun isMethodAllowed(inboundRequest: HttpServletRequest) =
        candidateConfig.allowedMethods.contains("*") or candidateConfig.allowedMethods.contains(inboundRequest.method)

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



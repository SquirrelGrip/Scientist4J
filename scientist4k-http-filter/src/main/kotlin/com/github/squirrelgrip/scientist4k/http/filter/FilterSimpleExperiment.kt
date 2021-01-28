package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.core.AbstractHttpSimpleExperiment
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.http.core.wrapper.ExperimentResponseWrapper
import com.google.common.eventbus.EventBus
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class FilterSimpleExperiment(
    experimentConfiguration: ExperimentConfiguration,
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    mappings: List<MappingConfiguration> = emptyList(),
    private val detourConfig: EndPointConfiguration
) : AbstractHttpSimpleExperiment(
    experimentConfiguration,
    eventBus
) {
    private val detourRequestFactory = RequestFactory(detourConfig, HttpExperimentUtil.DETOUR_COOKIE_STORE, mappings)

    fun run(
        inboundRequest: ServletRequest,
        inboundResponse: ServletResponse,
        chain: FilterChain
    ) {
        val sample: Sample = sampleFactory.create(getRunOptions(inboundRequest))
        val wrappedRequest = HttpServletRequestWrapper(inboundRequest as HttpServletRequest)
        val routeRequest = createRouteRequest(wrappedRequest, inboundResponse, chain)
        val detourRequest = createDetourRequest(wrappedRequest, sample)

        if (detourConfig.allowedMethods.contains("*") or detourConfig.allowedMethods.contains(inboundRequest.method)) {
            run(routeRequest, detourRequest, sample)
        } else {
            routeRequest.invoke()
        }
    }

    private fun createRouteRequest(
        wrappedRequest: HttpServletRequestWrapper,
        response: ServletResponse,
        chain: FilterChain
    ): () -> ExperimentResponse {
        return {
            val wrappedResponse = ExperimentResponseWrapper(response)
            chain.doFilter(wrappedRequest, wrappedResponse)
            wrappedResponse.experimentResponse
        }
    }

    private fun createDetourRequest(
        wrappedRequest: HttpServletRequestWrapper,
        sample: Sample
    ): () -> ExperimentResponse {
        val experimentRequest = HttpExperimentUtil.createRequest(wrappedRequest, sample)
        return detourRequestFactory.create(experimentRequest)
    }

}
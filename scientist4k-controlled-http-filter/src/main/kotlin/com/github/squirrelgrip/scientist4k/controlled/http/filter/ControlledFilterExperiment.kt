package com.github.squirrelgrip.scientist4k.controlled.http.filter

import com.github.squirrelgrip.scientist4k.controlled.ControlledExperiment
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.comparator.DefaultExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.http.core.wrapper.ExperimentResponseWrapper
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.google.common.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class ControlledFilterExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        comparator: ExperimentComparator<ExperimentResponse?> = DefaultExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        eventBus: EventBus = DEFAULT_EVENT_BUS,
        enabled: Boolean = true,
        async: Boolean = true,
        mappings: List<MappingConfiguration> = emptyList(),
        detourConfig: EndPointConfiguration,
        referenceConfig: EndPointConfiguration
) : ControlledExperiment<ExperimentResponse>(
        name,
        raiseOnMismatch,
        metrics,
        comparator,
        sampleFactory,
        eventBus,
        enabled,
        async
) {
    private val detourRequestFactory = RequestFactory(detourConfig, HttpExperimentUtil.DETOUR_COOKIE_STORE, mappings)
    private val referenceRequestFactory = RequestFactory(referenceConfig, HttpExperimentUtil.REFERENCE_COOKIE_STORE)

    private val allowedMethods = detourConfig.allowedMethods

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlledFilterExperiment::class.java)
    }

    fun run(
            inboundRequest: ServletRequest,
            inboundResponse: ServletResponse,
            chain: FilterChain,
            sample: Sample = sampleFactory.create()
    ) {
        val wrappedRequest = HttpServletRequestWrapper(inboundRequest as HttpServletRequest)
        val experimentRequest = HttpExperimentUtil.createRequest(wrappedRequest, sample)

        val routeRequest = createRouteRequest(wrappedRequest, inboundResponse, chain)
        val detourRequest = createDetourRequest(experimentRequest)
        val referenceRequest = createReferenceRequest(experimentRequest)

        if (allowedMethods.contains("*") or allowedMethods.contains(inboundRequest.method)) {
            run(routeRequest, referenceRequest, detourRequest, sample)
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
            experimentRequest: ExperimentRequest
    ): () -> ExperimentResponse {
        return detourRequestFactory.create(experimentRequest)
    }

    private fun createReferenceRequest(
            experimentRequest: ExperimentRequest
    ): () -> ExperimentResponse {
        return referenceRequestFactory.create(experimentRequest)
    }

}
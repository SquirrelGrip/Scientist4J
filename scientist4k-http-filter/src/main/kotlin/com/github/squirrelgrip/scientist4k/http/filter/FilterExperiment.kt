package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.Experiment
import com.github.squirrelgrip.scientist4k.core.ExperimentBuilder
import com.github.squirrelgrip.scientist4k.core.model.Mode
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.comparator.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class FilterExperiment : Filter {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(FilterExperiment::class.java)
    }

    lateinit var experiment: Experiment<ExperimentResponse>
    lateinit var alternateRouteRequestFactory: RequestFactory
    lateinit var mode: Mode

    override fun init(filterConfig: FilterConfig) {
        val config = filterConfig.getInitParameter("config")
        val filterExperimentConfiguration = File(config).toInstance<FilterExperimentConfiguration>()
        mode = filterExperimentConfiguration.mode
        experiment = ExperimentBuilder<ExperimentResponse>(filterExperimentConfiguration.experiment)
                .withComparator(ExperimentResponseComparator())
                .build()
        val mappings = filterExperimentConfiguration.mappings.map { (control, candidate) ->
            MappingConfiguration(control, candidate)
        }

        alternateRouteRequestFactory = RequestFactory(
                filterExperimentConfiguration.alternateRoute,
                "ROUTE_COOKIE_STORE",
                mappings
        )
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        when (mode) {
            Mode.NORMAL -> normalFilter(request, response, chain)
            Mode.SWAPPED -> swapFilter(request, response, chain)
            Mode.LOG_ONLY -> normalFilter(request, response, chain)
            else -> chain.doFilter(request, response)
        }
    }

    fun normalFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedRequest = HttpServletRequestWrapper(request as HttpServletRequest)
        val sample = experiment.sampleFactory.create()

        val controlResponse = createRouteResponse(wrappedRequest, response, chain)
        val candidateResponse = createAlternateRouteResponse(wrappedRequest, sample)

        experiment.run(controlResponse, candidateResponse, sample)
    }

    fun swapFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedRequest = HttpServletRequestWrapper(request as HttpServletRequest)
        val sample = experiment.sampleFactory.create()

        val controlResponse = createAlternateRouteResponse(wrappedRequest, sample)
        val candidateResponse = createRouteResponse(wrappedRequest, response, chain)

        experiment.run(controlResponse, candidateResponse, sample)
    }

    private fun createRouteResponse(
            wrappedRequest: HttpServletRequestWrapper,
            response: ServletResponse,
            chain: FilterChain
    ): () -> ExperimentResponse {
        return {
            val wrappedResponse = FilterExperimentResponseWrapper(response)
            chain.doFilter(wrappedRequest, wrappedResponse)
            wrappedResponse.experimentResponse
        }
    }

    private fun createAlternateRouteResponse(
            wrappedRequest: HttpServletRequestWrapper,
            sample: Sample
    ): () -> ExperimentResponse {
        val experimentRequest = HttpExperimentUtil.createRequest(wrappedRequest, sample)
        return alternateRouteRequestFactory.create(experimentRequest)
    }

}
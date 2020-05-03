package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.HttpExperimentUtil
import com.github.squirrelgrip.scientist4k.http.core.comparator.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.core.Experiment
import com.github.squirrelgrip.scientist4k.core.ExperimentBuilder
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
    lateinit var candidateRequestFactory: RequestFactory

    override fun init(filterConfig: FilterConfig) {
        filterConfig.initParameterNames.iterator().forEach {
            println("$it=${filterConfig.getInitParameter(it)}")
        }
        val config = filterConfig.getInitParameter("config")
        val filterExperimentConfiguration = File(config).toInstance<FilterExperimentConfiguration>()
        experiment = ExperimentBuilder<ExperimentResponse>(filterExperimentConfiguration.experiment)
                .withComparator(ExperimentResponseComparator())
                .build()
        candidateRequestFactory = RequestFactory(filterExperimentConfiguration.candidate, HttpExperimentUtil.CANDIDATE_COOKIE_STORE)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            val wrappedRequest = HttpServletRequestWrapper(request)
            val sample = experiment.sampleFactory.create()
            val experimentRequest = HttpExperimentUtil.createRequest(wrappedRequest, sample)
            val controlResponse = createControlResponse(response, chain, wrappedRequest)
            val candidateResponse = createCandidateResponse(experimentRequest)
            experiment.run(controlResponse, candidateResponse, sample)
        }
    }

    private fun createControlResponse(
            response: ServletResponse,
            chain: FilterChain,
            wrappedRequest: HttpServletRequestWrapper
    ): () -> ExperimentResponse {
        return {
            val wrappedResponse = FilterExperimentResponseWrapper(response)
            chain.doFilter(wrappedRequest, wrappedResponse)
            wrappedResponse.experimentResponse
        }
    }

    private fun createCandidateResponse(request: ExperimentRequest): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }

}
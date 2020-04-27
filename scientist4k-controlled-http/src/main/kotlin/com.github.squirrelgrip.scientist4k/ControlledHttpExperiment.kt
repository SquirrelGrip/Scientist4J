package com.github.squirrelgrip.scientist4k


import com.github.squirrelgrip.scientist4k.HttpExperiment.Companion.CANDIDATE_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.HttpExperiment.Companion.CONTROL_COOKIE_STORE
import com.github.squirrelgrip.scientist4k.HttpExperiment.Companion.processResponse
import com.github.squirrelgrip.scientist4k.comparator.ExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlledHttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<ExperimentResponse?> = ExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        private val mappings: List<MappingConfiguration> = emptyList(),
        private val controlConfig: EndPointConfiguration,
        private val referenceConfig: EndPointConfiguration,
        private val candidateConfig: EndPointConfiguration
) : ControlledExperiment<ExperimentResponse>(
        name,
        raiseOnMismatch,
        metrics,
        context,
        comparator,
        sampleFactory
) {
    private val controlRequestFactory = RequestFactory(controlConfig, CONTROL_COOKIE_STORE)
    private val referenceRequestFactory = RequestFactory(controlConfig, REFERENCE_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, CANDIDATE_COOKIE_STORE, mappings)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ControlledHttpExperiment::class.java)
        val REFERENCE_COOKIE_STORE = "REFERENCE_COOKIE_STORE"
    }

    init {
        addPublisher(object : ControlledPublisher<ExperimentResponse> {
            override fun publish(result: ControlledResult<ExperimentResponse>) {
                LOGGER.info("${result.match.matches} => ${result.sample.notes["uri"]}")
                if (!result.match.matches) {
                    LOGGER.info("\t${result.control.value}")
                    LOGGER.info("\t${result.reference.value}")
                    LOGGER.info("\t${result.candidate?.value}")
                    result.match.failureReasons.forEach {
                        LOGGER.info("\t\t${it}")
                    }
                }
            }
        })
    }

    fun run(
            inboundRequest: HttpServletRequest,
            inboundResponse: HttpServletResponse,
            sample: Sample = sampleFactory.create()
    ) {
        val experimentRequest = ExperimentRequest.create(inboundRequest)
        sample.addNote("request", experimentRequest.toString())
        sample.addNote("uri", experimentRequest.url)
        val controlResponse = if (candidateConfig.allowedMethods.contains("*") or candidateConfig.allowedMethods.contains(inboundRequest.method)) {
            run(createControlRequest(experimentRequest), createReferenceRequest(experimentRequest), createCandidateRequest(experimentRequest), sample)
        } else {
            createControlRequest(experimentRequest).invoke()
        }
        processResponse(inboundResponse, controlResponse)
    }

    private fun createControlRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return controlRequestFactory.create(request)
    }

    private fun createReferenceRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return referenceRequestFactory.create(request)
    }

    private fun createCandidateRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }
}



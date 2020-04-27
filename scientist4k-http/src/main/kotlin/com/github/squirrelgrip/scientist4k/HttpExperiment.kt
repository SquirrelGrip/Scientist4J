package com.github.squirrelgrip.scientist4k

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

class HttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<ExperimentResponse?> = ExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
        private val mappings: List<MappingConfiguration> = emptyList(),
        private val controlConfig: EndPointConfiguration,
        private val candidateConfig: EndPointConfiguration
) : Experiment<ExperimentResponse>(
        name,
        raiseOnMismatch,
        metrics,
        context,
        comparator,
        sampleFactory
) {
    private val controlRequestFactory = RequestFactory(controlConfig, CONTROL_COOKIE_STORE)
    private val candidateRequestFactory = RequestFactory(candidateConfig, CANDIDATE_COOKIE_STORE, mappings)

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(HttpExperiment::class.java)
        val CONTROL_COOKIE_STORE = "CONTROL_COOKIE_STORE"
        val CANDIDATE_COOKIE_STORE = "CANDIDATE_COOKIE_STORE"

        fun processResponse(
                inboundResponse: HttpServletResponse,
                controlResponse: ExperimentResponse?
        ) {
            LOGGER.debug("processing response {}", controlResponse)
            if (controlResponse != null) {
                inboundResponse.status = controlResponse.status.statusCode
                controlResponse.headers
                        .filter {
                            it.name != "Set-Cookie"
                        }
                        .forEach {
                            inboundResponse.addHeader(it.name, it.value)
                        }
                inboundResponse.outputStream.write(controlResponse.content)
            } else {
                LOGGER.warn("Control Response is null")
                inboundResponse.status = 500
                inboundResponse.writer.println("Something went wrong with the experiment")
            }
            inboundResponse.flushBuffer()
        }
    }

    init {
        addPublisher(object : Publisher<ExperimentResponse> {
            override fun publish(result: Result<ExperimentResponse>) {
                LOGGER.info("${result.match.matches} => ${result.sample.notes["uri"]}")
                if (!result.match.matches) {
                    LOGGER.info("\t${result.control.value}")
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
            run(createControlRequest(experimentRequest), createCandidateRequest(experimentRequest), sample)
        } else {
            createControlRequest(experimentRequest).invoke()
        }
        processResponse(inboundResponse, controlResponse)
    }

    private fun createControlRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return controlRequestFactory.create(request)
    }

    private fun createCandidateRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }

}



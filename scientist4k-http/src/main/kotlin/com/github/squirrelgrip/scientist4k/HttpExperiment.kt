package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.factory.RequestFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.*
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpExperiment(
        name: String,
        raiseOnMismatch: Boolean,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        context: Map<String, Any> = emptyMap(),
        comparator: ExperimentComparator<ExperimentResponse> = ExperimentResponseComparator(),
        sampleFactory: SampleFactory = SampleFactory(),
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
    private val controlRequestFactory = RequestFactory(controlConfig, "CONTROL_COOKIE_STORE")
    private val candidateRequestFactory = RequestFactory(candidateConfig, "CANDIDATE_COOKIE_STORE")

    init{
        addPublisher(object: Publisher<ExperimentResponse> {
            override fun publish(result: Result<ExperimentResponse>) {
                println("${result.match} => ${result.sample.notes["uri"]}")
                if (!result.match) {
                    println("\t${result.sample.notes["request"]}")
                    println("\t${result.control.value}")
                    println("\t${result.candidate?.value}")
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

    private fun processResponse(
            inboundResponse: HttpServletResponse,
            controlResponse: ExperimentResponse?
    ) {
        if (controlResponse != null) {
            inboundResponse.status = controlResponse.status.statusCode
            controlResponse.headers.forEach {
                inboundResponse.addHeader(it.name, it.value)
            }
            inboundResponse.outputStream.write(controlResponse.content)
        } else {
            inboundResponse.status = 500
            inboundResponse.writer.println("Something went wrong with the experiment")
        }
        inboundResponse.flushBuffer()
    }

    private fun createControlRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return controlRequestFactory.create(request)
    }

    private fun createCandidateRequest(request: ExperimentRequest): () -> ExperimentResponse {
        return candidateRequestFactory.create(request)
    }

}



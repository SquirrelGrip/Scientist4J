package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.model.ExperimentRequest
import com.github.squirrelgrip.scientist4k.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.model.sample.Sample
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object HttpExperimentUtil {
    private val LOGGER: Logger = LoggerFactory.getLogger(HttpExperimentUtil::class.java)
    val CONTROL_COOKIE_STORE = "CONTROL_COOKIE_STORE"
    val REFERENCE_COOKIE_STORE = "REFERENCE_COOKIE_STORE"
    val CANDIDATE_COOKIE_STORE = "CANDIDATE_COOKIE_STORE"

    fun createRequest(inboundRequest: HttpServletRequest, sample: Sample): ExperimentRequest {
        val experimentRequest = ExperimentRequest.create(inboundRequest)
        sample.addNote("request", experimentRequest.toString())
        sample.addNote("uri", experimentRequest.url)
        return experimentRequest
    }

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
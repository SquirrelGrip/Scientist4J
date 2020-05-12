package com.github.squirrelgrip.scientist4k.http.core.extension

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.Observation
import com.github.squirrelgrip.scientist4k.http.core.model.*

fun ExperimentResult<ExperimentResponse>.toHttpExperimentResult(): HttpExperimentResult {
    val experimentName = this.sample.notes["experiment"] ?: "unknown"
    val request = this.sample.notes["request"]!!.toInstance<HttpExperimentRequest>()
    val responses = mapOf("control" to this.control, "candidate" to this.candidate)
            .filterValues { it != null }
            .map { (key, value) -> key to value!!.toHttpExperimentObservation() }
            .toMap()
    return HttpExperimentResult(
            this.sample.id,
            this.sample.startTime,
            experimentName,
            request,
            responses
    )
}

fun ExperimentRequest.toHttpExperimentRequest(): HttpExperimentRequest {
    return HttpExperimentRequest(method, uri, headers, parameters, String(body))
}

fun Observation<ExperimentResponse>.toHttpExperimentObservation(): HttpExperimentObservation {
    return HttpExperimentObservation(this.value!!.toHttpExperimentResponse(), this.duration)
}

fun ExperimentResponse.toHttpExperimentResponse(): HttpExperimentResponse {
    return HttpExperimentResponse(status, headers, contentType ?: "", contents)
}

package com.github.squirrelgrip.scientist4k.http.core.extension

import com.github.squirrelgrip.scientist4k.controlled.model.ControlledExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.http.core.comparator.DefaultHttpExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.comparator.HttpExperimentResponseComparator
import com.github.squirrelgrip.scientist4k.http.core.model.*
import com.github.squirrelgrip.scientist4k.simple.model.ExperimentResult

fun ExperimentResult<ExperimentResponse>.toHttpExperimentResult(): HttpExperimentResult {
    val experimentName = (this.sample.notes["experiment"] ?: "unknown").toString()
    val request = (this.sample.notes["request"] as ExperimentRequest).toHttpExperimentRequest()
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

fun ControlledExperimentResult<ExperimentResponse>.toHttpExperimentResult(): HttpExperimentResult {
    val experimentName = (this.sample.notes["experiment"] ?: "unknown").toString()
    val request = (this.sample.notes["request"] as ExperimentRequest).toHttpExperimentRequest()
    val responses = mapOf("control" to this.control, "candidate" to this.candidate, "reference" to this.reference)
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

fun ExperimentObservation<ExperimentResponse>.toHttpExperimentObservation(): HttpExperimentObservation {
    return HttpExperimentObservation(this.value!!.toHttpExperimentResponse(), this.duration)
}

fun ExperimentResponse.toHttpExperimentResponse(): HttpExperimentResponse {
    return HttpExperimentResponse(status, headers, contentType ?: "", contents)
}

fun HttpExperimentResult.compare(comparator: HttpExperimentResponseComparator = DefaultHttpExperimentResponseComparator()): ComparisonResult {
    return comparator.invoke(responses["control"]!!.response, responses["candidate"]!!.response)
}

fun HttpExperimentResult.matches(comparator: HttpExperimentResponseComparator = DefaultHttpExperimentResponseComparator()): Boolean {
    return this.compare(comparator).matches
}

fun HttpExperimentResult.failureReasons(comparator: HttpExperimentResponseComparator = DefaultHttpExperimentResponseComparator()): List<String> {
    return this.compare(comparator).failureReasons
}
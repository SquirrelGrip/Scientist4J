package com.github.squirrelgrip.scientist4k.http.core.model

import java.time.Instant

class HttpExperimentResult(
        val id: String,
        val startTime: Instant,
        val experiment: String,
        val request: HttpExperimentRequest,
        val responses: List<HttpExperimentObservation> = emptyList()
) {
    operator fun get(name: String): HttpExperimentObservation? =
            responses.firstOrNull { it.name == name }
}
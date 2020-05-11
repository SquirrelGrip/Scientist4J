package com.github.squirrelgrip.scientist4k.http.core.model

import java.time.Instant

data class HttpExperimentResult(
        val id: String,
        val startTime: Instant,
        val experiment: String,
        val request: HttpExperimentRequest,
        val responses: Map<String, HttpExperimentObservation> = emptyMap()
)
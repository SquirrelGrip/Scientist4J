package com.github.squirrelgrip.scientist4k.http.core.model

import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservationType
import java.time.Instant

class HttpExperimentResult(
        val id: String,
        val startTime: Instant,
        val experiment: String,
        val request: HttpExperimentRequest,
        val responses: List<HttpExperimentObservation> = emptyList()
) {
    operator fun get(type: ExperimentObservationType): HttpExperimentObservation? =
            responses.firstOrNull { it.type == type }
}
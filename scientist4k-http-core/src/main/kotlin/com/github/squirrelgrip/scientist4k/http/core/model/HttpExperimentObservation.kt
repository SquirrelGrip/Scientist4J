package com.github.squirrelgrip.scientist4k.http.core.model

data class HttpExperimentObservation(
        val response: HttpExperimentResponse,
        val duration: Long
)
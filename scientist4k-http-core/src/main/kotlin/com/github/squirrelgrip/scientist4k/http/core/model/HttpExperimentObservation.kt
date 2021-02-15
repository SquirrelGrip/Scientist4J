package com.github.squirrelgrip.scientist4k.http.core.model

import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservationType

data class HttpExperimentObservation(
        val type: ExperimentObservationType,
        val response: HttpExperimentResponse,
        val duration: Long
)
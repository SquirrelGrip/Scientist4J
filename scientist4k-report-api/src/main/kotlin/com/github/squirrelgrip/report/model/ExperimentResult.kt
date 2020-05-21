package com.github.squirrelgrip.report.model

import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult

data class ExperimentResult(
        val match: Boolean,
        val httpExperimentResult: HttpExperimentResult
)
package com.github.squirrelgrip.report.model

import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult

interface ExperimentResult {
    val name: String
    val results: List<String>

    fun getResult(id: String): HttpExperimentResult
}

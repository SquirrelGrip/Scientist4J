package com.github.squirrelgrip.app.api.model

import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult

interface ExperimentResults {
    val name: String
    val results: List<String>

    fun getResult(id: String): HttpExperimentResult
}

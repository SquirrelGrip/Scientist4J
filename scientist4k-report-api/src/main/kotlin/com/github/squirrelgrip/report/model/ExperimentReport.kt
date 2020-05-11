package com.github.squirrelgrip.report.model

import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult

data class ExperimentReport(
        val name: String,
        val results: List<HttpExperimentResult> = emptyList()
) {
    val groupByUri: Map<String, List<HttpExperimentResult>> =
            results.groupBy { it.experiment }
}

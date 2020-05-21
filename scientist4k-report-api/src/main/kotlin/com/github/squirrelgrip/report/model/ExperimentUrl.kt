package com.github.squirrelgrip.report.model

data class ExperimentUrl(
        val method: String,
        val uri: String,
        val passCount: Int = 0,
        val failCount: Int = 0,
        val experimentResults: List<ExperimentResult> = emptyList()
)
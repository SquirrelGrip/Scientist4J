package com.github.squirrelgrip.report.model

data class ExperimentDetails(
        val name: String,
        val urls: List<ExperimentUrl> = listOf()
)

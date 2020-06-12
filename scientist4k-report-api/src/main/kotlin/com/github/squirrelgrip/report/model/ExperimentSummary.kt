package com.github.squirrelgrip.report.model

class ExperimentSummary(
    val name: String,
    val lastUpdate: Long = 0,
    val count: Int = 0,
    val pass: Int = 0,
    val fail: Int = 0
)
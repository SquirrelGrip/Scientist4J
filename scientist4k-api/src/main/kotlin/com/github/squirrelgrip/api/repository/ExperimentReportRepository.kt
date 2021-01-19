package com.github.squirrelgrip.api.repository

import com.github.squirrelgrip.api.model.ExperimentResult

interface ExperimentReportRepository {
    fun findAllExperiments(): List<ExperimentResult>
    fun findExperimentByName(name: String): ExperimentResult
}

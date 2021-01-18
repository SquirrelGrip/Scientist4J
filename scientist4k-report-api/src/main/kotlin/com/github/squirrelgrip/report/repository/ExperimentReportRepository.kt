package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.model.ExperimentResult

interface ExperimentReportRepository {
    fun findAllExperiments(): List<ExperimentResult>
    fun findExperimentByName(name: String): ExperimentResult
}

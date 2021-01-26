package com.github.squirrelgrip.app.api.repository

import com.github.squirrelgrip.app.api.model.ExperimentResults

interface ExperimentReportRepository {
    fun findAllExperiments(): List<ExperimentResults>
    fun findExperimentByName(name: String): ExperimentResults
}

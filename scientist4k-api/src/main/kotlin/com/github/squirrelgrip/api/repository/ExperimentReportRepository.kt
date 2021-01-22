package com.github.squirrelgrip.api.repository

import com.github.squirrelgrip.api.model.ExperimentResults

interface ExperimentReportRepository {
    fun findAllExperiments(): List<ExperimentResults>
    fun findExperimentByName(name: String): ExperimentResults
}

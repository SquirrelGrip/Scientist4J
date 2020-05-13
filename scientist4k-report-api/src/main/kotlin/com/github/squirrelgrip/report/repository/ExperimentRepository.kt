package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.model.ExperimentReport
import com.github.squirrelgrip.report.model.ExperimentSummary

interface ExperimentRepository {
    fun findAllExperiments(): List<ExperimentSummary>
    fun findExperimentByName(name: String): ExperimentReport
}

package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.model.ExperimentReport

interface ExperimentRepository {
    fun findAllExperiments(): List<String>
    fun findExperimentByName(name: String): ExperimentReport
}

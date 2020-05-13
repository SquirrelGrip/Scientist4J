package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.model.Experiment

interface ExperimentRepository {
    fun findAllExperiments(): List<Experiment>
    fun findExperimentByName(name: String): Experiment
}

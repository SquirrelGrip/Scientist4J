package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.exception.ExperimentNotFoundException
import com.github.squirrelgrip.report.model.Experiment
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentRepository(
        val baseDirectory: File
) : ExperimentRepository {
    val experiments : MutableMap<String, Experiment> = mutableMapOf()

    override fun findAllExperiments(): List<Experiment> {
        update()
        return experiments.values.toList()
    }

    override fun findExperimentByName(name: String): Experiment {
        update()
        return experiments[name] ?: throw ExperimentNotFoundException()
    }

    fun update() {
        baseDirectory
                .listFiles { file -> file.isDirectory() }
                .map { Experiment(it) }
                .filter { !experiments.containsKey(it.name) }
                .forEach { experiments[it.name] = it }
    }
}
package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.report.exception.ExperimentNotFoundException
import com.github.squirrelgrip.report.model.file.FileExperiment
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentRepository(
        val baseDirectory: File
) : ExperimentRepository {
    private val experiments : MutableMap<String, FileExperiment> = mutableMapOf()

    override fun findAllExperiments(): List<FileExperiment> {
        update()
        return experiments.values.toList()
    }

    override fun findExperimentByName(name: String): FileExperiment {
        update()
        return experiments[name] ?: throw ExperimentNotFoundException()
    }

    fun update() {
        baseDirectory
                .listFiles { file -> file.isDirectory() }
                .map { FileExperiment(it) }
                .filter { !experiments.containsKey(it.name) }
                .forEach { experiments[it.name] = it }
    }
}
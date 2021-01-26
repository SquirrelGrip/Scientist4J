package com.github.squirrelgrip.app.api.repository

import com.github.squirrelgrip.app.common.exception.ExperimentNotFoundException
import com.github.squirrelgrip.app.api.model.file.FileExperimentResults
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentReportRepository(
    val baseDirectory: File
) : ExperimentReportRepository {
    private val experiments: MutableMap<String, FileExperimentResults> = mutableMapOf()

    override fun findAllExperiments(): List<FileExperimentResults> {
        update()
        return experiments.values.toList()
    }

    override fun findExperimentByName(name: String): FileExperimentResults {
        update()
        return experiments[name] ?: throw ExperimentNotFoundException()
    }

    fun update() {
        baseDirectory
            .listFiles { file -> file.isDirectory() }
            .map { FileExperimentResults(it) }
            .filter { !experiments.containsKey(it.name) }
            .forEach { experiments[it.name] = it }
    }
}
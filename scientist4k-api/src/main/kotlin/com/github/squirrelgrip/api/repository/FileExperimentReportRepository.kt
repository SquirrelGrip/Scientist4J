package com.github.squirrelgrip.api.repository

import com.github.squirrelgrip.api.exception.ExperimentNotFoundException
import com.github.squirrelgrip.api.model.file.FileExperimentResult
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentReportRepository(
        val baseDirectory: File
) : ExperimentReportRepository {
    private val experiments : MutableMap<String, FileExperimentResult> = mutableMapOf()

    override fun findAllExperiments(): List<FileExperimentResult> {
        update()
        return experiments.values.toList()
    }

    override fun findExperimentByName(name: String): FileExperimentResult {
        update()
        return experiments[name] ?: throw ExperimentNotFoundException()
    }

    fun update() {
        baseDirectory
                .listFiles { file -> file.isDirectory() }
                .map { FileExperimentResult(it) }
                .filter { !experiments.containsKey(it.name) }
                .forEach { experiments[it.name] = it }
    }
}
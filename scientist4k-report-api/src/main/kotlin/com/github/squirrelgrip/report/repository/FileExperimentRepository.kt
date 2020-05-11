package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.report.exception.ExperimentNotFoundException
import com.github.squirrelgrip.report.model.ExperimentReport
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentRepository(
        val baseDirectory: File
) : ExperimentRepository {
    override fun findAllExperiments(): List<String> {
        return baseDirectory.list { dir, _ ->
            dir.isDirectory()
        }.toList()
    }

    override fun findExperimentByName(name: String): ExperimentReport {
        val experimentDirectory = File(baseDirectory, name)
        if (experimentDirectory.exists()) {
            val list = experimentDirectory.listFiles().map {
                it.toInstance<ExperimentResult<ExperimentResponse>>()
            }
            return ExperimentReport(name, list)
        }
        throw ExperimentNotFoundException()
    }
}
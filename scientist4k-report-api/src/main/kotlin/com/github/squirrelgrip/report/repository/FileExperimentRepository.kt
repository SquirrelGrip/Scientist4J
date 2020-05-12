package com.github.squirrelgrip.report.repository

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.report.exception.ExperimentNotFoundException
import com.github.squirrelgrip.report.model.ExperimentReport
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import org.springframework.stereotype.Repository
import java.io.File

@Repository
class FileExperimentRepository(
        val baseDirectory: File
) : ExperimentRepository {
   override fun findAllExperiments(): List<String> {
        return baseDirectory.list { file, _ ->
            file.isDirectory()
        }.toList()
    }

    override fun findExperimentByName(name: String): ExperimentReport {
        val experimentDirectory = File(baseDirectory, name)
        if (experimentDirectory.exists() && experimentDirectory.isDirectory()) {
            val list = experimentDirectory.listFiles().map {
                it.toInstance<HttpExperimentResult>()
            }
            return ExperimentReport(name, list)
        }
        throw ExperimentNotFoundException()
    }
}
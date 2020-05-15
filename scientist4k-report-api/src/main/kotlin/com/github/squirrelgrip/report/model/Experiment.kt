package com.github.squirrelgrip.report.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.report.exception.ExperimentResultNotFoundException
import com.github.squirrelgrip.scientist4k.http.core.extension.matches
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import java.io.File

data class Experiment(
        @JsonIgnore
        val experimentDirectory: File
) {
    val name: String = experimentDirectory.name
    private var lastUpdate: Long = 0
    private val results: MutableList<HttpExperimentResult> = mutableListOf()

    init {
        update()
    }

    fun update() {
        experimentDirectory.listFiles { file, _ ->
            file.lastModified() > lastUpdate
        }.forEach {
            if (it.lastModified() > lastUpdate) {
                lastUpdate = it.lastModified()
            }
            results.add(it.toInstance())
        }
    }

    fun findById(id: String): HttpExperimentResult {
        return results.firstOrNull { it.id == id } ?: throw ExperimentResultNotFoundException()
    }

    val experimentSummary: ExperimentSummary
        get() = {
            update()
            val matches = results.partition {
                it.matches()
            }
            val pass = matches.first.size
            val fail = matches.second.size
            ExperimentSummary(name, lastUpdate, results.size, pass, fail)
        }.invoke()

    val experimentUrls: List<ExperimentUrl>
        get() = {
            update()
            results.groupBy {
                it.request.method to it.request.url
            }.map {(key, value) ->
                val partition = value.partition { it.matches() }
                val ids = value.map { it.id }
                ExperimentUrl(key.first, key.second, partition.first.size, partition.second.size, ids)
            }
        }.invoke()
}

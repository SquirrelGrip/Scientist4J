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
            val matches = results.map {
                it.matches()
            }
            val pass = matches.count { it }
            val fail = matches.count { !it }
            ExperimentSummary(name, lastUpdate, results.size, pass, fail)
        }.invoke()

    val experimentUriSummary: Map<String, Map<String, Boolean>>
        get() = {
            update()
            results.groupBy {
                "${it.request.method} ${it.request.url}"
            }.map {(key, value) ->
                key to value.map {
                    it.id to it.matches()
                }.toMap()
            }.toMap()
        }.invoke()
}

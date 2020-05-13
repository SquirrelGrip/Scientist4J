package com.github.squirrelgrip.report.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.extension.matches
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import java.io.File

class ExperimentSummary(
        @JsonIgnore
        val experimentDirectory: File
) {
    val name: String = experimentDirectory.name
    var lastUpdate: Long = 0
    var count: Int = 0
    var pass: Int = 0
    var fail: Int = 0

    init {
        updateData()
    }

    fun updateData() {
        val files = experimentDirectory.listFiles { file, _ ->
            file.lastModified() > lastUpdate
        }.map {
            if (it.lastModified() > lastUpdate) {
                lastUpdate = it.lastModified()
            }
            it.toInstance<HttpExperimentResult>()
        }.groupBy {
            it.matches()
        }
        pass = (files[true]?:emptyList()).size
        fail = (files[false]?:emptyList()).size
        count = pass + fail
    }

}

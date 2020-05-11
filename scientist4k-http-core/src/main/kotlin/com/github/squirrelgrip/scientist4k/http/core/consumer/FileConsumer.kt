package com.github.squirrelgrip.scientist4k.http.core.consumer

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.eventbus.Subscribe
import java.io.File

class FileConsumer(
        val baseDirectory: File
) {
    @Subscribe
    fun receiveResult(experimentResult: ExperimentResult<ExperimentResponse>) {
        val experimentName = experimentResult.sample.notes["experiment"]
        val sampleId = experimentResult.sample.id
        val file = File(File(baseDirectory, experimentName).apply {
            mkdirs()
        }, "$sampleId.json")
        experimentResult.toHttpExperimentResult().toJson(file)
    }
}

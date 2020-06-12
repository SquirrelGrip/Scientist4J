package com.github.squirrelgrip.scientist4k.http.core.consumer

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import com.google.common.eventbus.Subscribe
import java.io.File

class FileConsumer(
        val baseDirectory: File
): Consumer<HttpExperimentResult> {
    @Subscribe
    override fun receiveResult(experimentResult: HttpExperimentResult) {
        val experimentName = experimentResult.experiment
        val sampleId = experimentResult.id
        val experimentDirectory = File(baseDirectory, experimentName).apply {
            mkdirs()
        }
        val file = File(experimentDirectory, "$sampleId.json")
        experimentResult.toJson(file)
    }
}

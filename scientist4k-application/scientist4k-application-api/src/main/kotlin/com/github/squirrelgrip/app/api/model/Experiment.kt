package com.github.squirrelgrip.app.api.model

import com.github.squirrelgrip.scientist4k.http.server.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.server.HttpExperimentServer

class Experiment(
    val configuration: HttpExperimentConfiguration
) {
    private val server: HttpExperimentServer = HttpExperimentServer(configuration)

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    val state: String
        get() = server.server.state
}

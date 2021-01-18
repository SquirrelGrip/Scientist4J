package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.configuration.HttpExperimentConfiguration
import java.io.File

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val controlledHttpExperimentConfiguration = File("controlled-experiment-config.json").toInstance<HttpExperimentConfiguration>()
            val server = ControlledHttpExperimentServer(
                    controlledHttpExperimentConfiguration
            )
            server.start()
            server.join()
        }
    }
}
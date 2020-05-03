package com.github.squirrelgrip.scientist4k.controlled.http

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.configuration.ControlledHttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import java.io.File

class ControlledHttpExperimentServer(
        val controlledHttpExperimentConfiguration: ControlledHttpExperimentConfiguration
): SecuredServer(controlledHttpExperimentConfiguration.server, ControlledExperimentHandler(controlledHttpExperimentConfiguration))

fun main() {
    val httpExperimentConfiguration = File("controlled-experiment-config.json").toInstance<ControlledHttpExperimentConfiguration>()
    val server = ControlledHttpExperimentServer(
            httpExperimentConfiguration
    )
    server.start()
    server.join()
}

package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.ControlledHttpExperimentConfiguration
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

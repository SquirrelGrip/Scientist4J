package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.configuration.ControlledHttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import java.io.File

class ControlledHttpExperimentServer(
        serverConfiguration: ServerConfiguration,
        httpExperimentHandler: ControlledHttpExperimentHandler
): SecuredServer(serverConfiguration, httpExperimentHandler) {
    constructor(
            controlledHttpExperimentConfiguration: ControlledHttpExperimentConfiguration
    ): this(controlledHttpExperimentConfiguration.server, ControlledHttpExperimentHandler(controlledHttpExperimentConfiguration))

    constructor(
            serverConfiguration: ServerConfiguration,
            httpExperiment: ControlledHttpExperiment
    ): this(serverConfiguration, ControlledHttpExperimentHandler(httpExperiment))
}

fun main() {
    val controlledHttpExperimentConfiguration = File("controlled-experiment-config.json").toInstance<ControlledHttpExperimentConfiguration>()
    val server = ControlledHttpExperimentServer(
            controlledHttpExperimentConfiguration
    )
    server.start()
    server.join()
}

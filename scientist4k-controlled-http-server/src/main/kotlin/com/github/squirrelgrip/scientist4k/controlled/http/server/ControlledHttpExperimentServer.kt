package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.http.core.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer

class ControlledHttpExperimentServer(
        serverConfiguration: ServerConfiguration,
        httpExperimentHandler: ControlledHttpExperimentHandler
): SecuredServer(serverConfiguration, httpExperimentHandler) {
    constructor(
            httpExperimentConfiguration: HttpExperimentConfiguration
    ): this(httpExperimentConfiguration.server, ControlledHttpExperimentHandler(httpExperimentConfiguration))

    constructor(
            serverConfiguration: ServerConfiguration,
            httpExperiment: ControlledHttpExperiment
    ): this(serverConfiguration, ControlledHttpExperimentHandler(httpExperiment))
}

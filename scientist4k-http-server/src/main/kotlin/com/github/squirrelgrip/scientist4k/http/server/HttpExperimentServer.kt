package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer

class HttpExperimentServer(
    serverConfiguration: ServerConfiguration,
    httpExperimentHandler: HttpExperimentHandler
) : SecuredServer(serverConfiguration, httpExperimentHandler) {
    constructor(
        httpExperimentConfiguration: HttpExperimentConfiguration
    ) : this(
        httpExperimentConfiguration.server,
        HttpExperimentHandler(httpExperimentConfiguration)
    )

    constructor(
        serverConfiguration: ServerConfiguration,
        httpExperiment: SimpleHttpExperiment
    ) : this(
        serverConfiguration,
        HttpExperimentHandler(httpExperiment)
    )

}


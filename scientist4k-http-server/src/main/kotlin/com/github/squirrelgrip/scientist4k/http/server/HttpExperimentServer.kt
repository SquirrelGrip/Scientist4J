package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import java.io.File

class HttpExperimentServer(
        serverConfiguration: ServerConfiguration,
        httpExperimentHandler: HttpExperimentHandler
): SecuredServer(serverConfiguration, httpExperimentHandler) {
    constructor(
            httpExperimentConfiguration: HttpExperimentConfiguration
    ): this(httpExperimentConfiguration.server, HttpExperimentHandler(httpExperimentConfiguration))

    constructor(
            serverConfiguration: ServerConfiguration,
            httpExperiment: HttpSimpleExperiment
    ): this(serverConfiguration, HttpExperimentHandler(httpExperiment))
}

fun main() {
    val httpExperimentConfiguration = File("experiment-config.json").toInstance<HttpExperimentConfiguration>()
    val server = HttpExperimentServer(
            httpExperimentConfiguration
    )
    server.start()
    server.join()
}

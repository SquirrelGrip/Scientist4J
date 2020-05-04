package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import java.io.File

class HttpExperimentServer(
        val httpExperimentConfiguration: HttpExperimentConfiguration
): SecuredServer(httpExperimentConfiguration.server, ExperimentHandler(httpExperimentConfiguration))

fun main() {
    val httpExperimentConfiguration = File("experiment-config.json").toInstance<HttpExperimentConfiguration>()
    val server = HttpExperimentServer(
            httpExperimentConfiguration
    )
    server.start()
    server.join()
}

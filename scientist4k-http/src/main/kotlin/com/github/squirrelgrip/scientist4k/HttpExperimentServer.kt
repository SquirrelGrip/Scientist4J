package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.server.SecuredServer
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

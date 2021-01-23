package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.extension.json.toInstance
import java.io.File

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val httpExperimentConfiguration = File("experiment-config.json").toInstance<HttpExperimentConfiguration>()
            val server = HttpExperimentServer(
                    httpExperimentConfiguration
            )
            server.start()
            server.join()
        }

    }
}
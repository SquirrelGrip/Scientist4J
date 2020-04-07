package com.github.squirrelgrip.scientist4k.handler

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.HttpExperiment
import com.github.squirrelgrip.scientist4k.HttpExperimentBuilder
import com.github.squirrelgrip.scientist4k.SecuredServer
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ExperimentHandler(
        val experiment: HttpExperiment
) : AbstractHandler() {

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration): this(HttpExperimentBuilder(httpExperimentConfiguration).build())
    constructor(file: File): this(file.toInstance<HttpExperimentConfiguration>())

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        experiment.run(request, response)
        baseRequest.isHandled = true
    }

}

fun main() {
    val sslConfiguration = File("config.json").toInstance<SslConfiguration>()
    val handler = ExperimentHandler(File("experiment-config.json"))
    val server = SecuredServer(
            8999,
            9000,
            handler,
            sslConfiguration
    )
    server.start()
    server.join()
}


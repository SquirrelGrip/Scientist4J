package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ExperimentHandler(
        val experiment: HttpExperiment
) : AbstractHandler() {

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration): this(HttpExperimentBuilder(httpExperimentConfiguration).build())

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

package com.github.squirrelgrip.scientist4k.controlled.http.server

import com.github.squirrelgrip.scientist4k.http.core.configuration.HttpExperimentConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlledHttpExperimentHandler(
        val controlledHttpExperiment: ControlledHttpExperiment
) : AbstractHandler() {

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration): this(ControlledHttpExperimentBuilder(httpExperimentConfiguration).build())

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        controlledHttpExperiment.run(request, response)
        baseRequest.isHandled = true
    }

}

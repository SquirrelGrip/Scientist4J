package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.ControlledHttpExperimentConfiguration
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ControlledExperimentHandler(
        val controlledHttpExperiment: ControlledHttpExperiment
) : AbstractHandler() {

    constructor(controlledHttpExperimentConfiguration: ControlledHttpExperimentConfiguration): this(ControlledHttpExperimentBuilder(controlledHttpExperimentConfiguration).build())

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

package com.github.squirrelgrip.scientist4k.http.server

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class HttpExperimentHandler(
        val httpExperiment: SimpleHttpExperiment
) : AbstractHandler() {

    constructor(httpExperimentConfiguration: HttpExperimentConfiguration): this(HttpExperimentBuilder(httpExperimentConfiguration).build())

    override fun handle(
            target: String,
            baseRequest: Request,
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        httpExperiment.run(request, response)
        baseRequest.isHandled = true
    }

}

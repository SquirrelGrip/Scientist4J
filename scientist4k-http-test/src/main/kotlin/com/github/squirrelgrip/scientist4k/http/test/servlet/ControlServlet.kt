package com.github.squirrelgrip.scientist4k.http.test.servlet

import com.github.squirrelgrip.scientist4k.http.core.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.ControlHandler
import com.github.squirrelgrip.scientist4k.http.test.handler.Main
import org.eclipse.jetty.servlet.ServletContextHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ControlServlet : HttpServlet() {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CandidateServlet::class.java)

        val serverConfiguration = ServerConfiguration(
                listOf(
                        ConnectorConfiguration(9003),
                        ConnectorConfiguration(9004,
                                Main.sslConfiguration
                        )
                )
        )
    }

    override fun doGet(
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val target = request.servletPath
        ControlHandler.handleRequest(request, response, target)
    }
}

fun main() {
    val context = ServletContextHandler(ServletContextHandler.SESSIONS).apply {
        contextPath = "/"
        addServlet(ControlServlet::class.java, "/")
    }
    val server = SecuredServer(ControlServlet.serverConfiguration, context)
    server.start()
    server.join()
}
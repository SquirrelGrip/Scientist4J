package com.github.squirrelgrip.scientist4k.http.test.servlet

import com.github.squirrelgrip.scientist4k.http.core.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.Main
import com.github.squirrelgrip.scientist4k.http.test.handler.ReferenceHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ReferenceServlet : HttpServlet() {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ReferenceServlet::class.java)

        val serverConfiguration = ServerConfiguration(
                listOf(
                        ConnectorConfiguration(9023),
                        ConnectorConfiguration(9024,
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
        ReferenceHandler.handleRequest(request, response, target)
    }
}

fun main() {
    val context = ServletContextHandler(ServletContextHandler.SESSIONS).apply {
        contextPath = "/"
        addServlet(ReferenceServlet::class.java, "/")
    }
    val server = SecuredServer(ReferenceServlet.serverConfiguration, context)
    server.start()
    server.join()
}
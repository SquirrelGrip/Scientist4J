package com.github.squirrelgrip.scientist4k.http.core.server

import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.session.DefaultSessionIdManager
import org.eclipse.jetty.server.session.SessionHandler

open class SecuredServer(
        val serverConfiguration: ServerConfiguration,
        val handler: Handler? = null
) {
    val server = Server()

    init {
        val serverConnectors = serverConfiguration.getServerConnectors(server)
        server.connectors = serverConnectors
        server.sessionIdManager = DefaultSessionIdManager(server)
        if (handler != null) {
            if (handler is ContextHandler) {
                server.handler = handler
            } else {
                ContextHandler("/").also { contextHandler ->
                    server.handler = contextHandler
                    SessionHandler().also { sessionHandler ->
                        contextHandler.handler = sessionHandler
                        sessionHandler.handler = handler
                    }
                }
            }
        }
    }

    fun start() {
        server.start()
    }

    fun join() {
        server.join()
    }

    fun stop() {
        server.stop()
    }


}
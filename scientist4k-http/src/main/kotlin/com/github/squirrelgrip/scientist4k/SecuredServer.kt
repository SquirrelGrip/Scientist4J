package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.configuration.SslConfiguration
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.session.DefaultSessionIdManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.util.ssl.SslContextFactory

class SecuredServer(
        val httpPort: Int?,
        val httpsPort: Int?,
        val handler: Handler,
        val sslConfiguration: SslConfiguration?
) {
    val server = Server()

    init {
        server.connectors = listOfNotNull(createHttpConnector(), createHttpsConnector()).toTypedArray()
        server.sessionIdManager = DefaultSessionIdManager(server)
        ContextHandler("/").also { contextHandler ->
            server.handler = contextHandler
            SessionHandler().also { sessionHandler ->
                contextHandler.handler = sessionHandler
                sessionHandler.handler = handler
            }
        }
    }

    private fun createHttpConnector(): ServerConnector? {
        if (httpPort != null) {
            return ServerConnector(server).apply {
                port = httpPort
            }
        }
        return null
    }

    private fun createHttpsConnector(): ServerConnector? {
        if (httpsPort != null && sslConfiguration != null) {
            val httpsConfiguration = HttpConfiguration().apply {
                addCustomizer(SecureRequestCustomizer())
            }
            val sslContextFactory = SslContextFactory.Server().apply {
                keyStorePath = sslConfiguration.keyStorePath
                trustStorePath = sslConfiguration.trustStorePath
                setKeyStorePassword(sslConfiguration.keyStorePassword)
                setKeyManagerPassword(sslConfiguration.keyStorePassword)
                setTrustStorePassword(sslConfiguration.trustStorePassword)
            }
            return ServerConnector(server,
                    SslConnectionFactory(sslContextFactory, "http/1.1"),
                    HttpConnectionFactory(httpsConfiguration)).apply {
                port = httpsPort
            }
        }
        return null
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
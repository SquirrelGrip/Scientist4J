package com.github.squirrelgrip.scientist4k.configuration

import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory

data class ServerConfiguration(
        val connectors: List<ConnectorConfiguration>
) {
    fun getServerConnectors(server: Server): Array<ServerConnector> {
        return connectors.map {
            val serverConnector = createServerConnector(it, server)
            serverConnector.apply {
                port = it.port
            }
        }.toTypedArray()
    }

    private fun createServerConnector(it: ConnectorConfiguration, server: Server): ServerConnector {
        return if (it.sslConfiguration != null) {
            createSslServerConnector(it.sslConfiguration, server)
        } else {
            createServerConnector(server)
        }
    }

    private fun createServerConnector(server: Server) = ServerConnector(server)

    private fun createSslServerConnector(sslConfiguration: SslConfiguration, server: Server): ServerConnector {
        val httpsConfiguration = HttpConfiguration().apply {
            addCustomizer(SecureRequestCustomizer())
        }
        val sslContextFactory = SslContextFactory.Server().apply {
            keyStore = sslConfiguration.keyStore()
            trustStore = sslConfiguration.trustStore()
            setKeyManagerPassword(sslConfiguration.keyStorePassword)
        }
        return ServerConnector(server,
                SslConnectionFactory(sslContextFactory, "http/1.1"),
                HttpConnectionFactory(httpsConfiguration))
    }

}

package com.github.squirrelgrip.scientist4k.configuration

data class ConnectorConfiguration(
        val port: Int,
        val sslConfiguration: SslConfiguration? = null
)

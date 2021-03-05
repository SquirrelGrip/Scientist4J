package com.github.squirrelgrip.scientist4k.http.core.configuration

data class ConnectorConfiguration(
    val port: Int,
    val sslConfiguration: SslConfiguration? = null
)

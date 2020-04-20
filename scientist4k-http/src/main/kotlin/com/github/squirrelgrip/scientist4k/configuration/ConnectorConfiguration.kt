package com.github.squirrelgrip.scientist4k.configuration

import com.github.squirrelgrip.configuration.ssl.SslConfiguration

data class ConnectorConfiguration(
        val port: Int,
        val sslConfiguration: SslConfiguration? = null
)

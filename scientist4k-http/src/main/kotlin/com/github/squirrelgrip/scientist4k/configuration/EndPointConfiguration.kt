package com.github.squirrelgrip.scientist4k.configuration

import com.github.squirrelgrip.configuration.ssl.SslConfiguration

data class EndPointConfiguration(
    val url: String,
    val allowedMethods: List<String> = listOf("GET"),
    val sslConfiguration: SslConfiguration? = null
)

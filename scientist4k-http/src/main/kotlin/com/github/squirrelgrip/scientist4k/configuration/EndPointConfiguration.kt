package com.github.squirrelgrip.scientist4k.configuration

data class EndPointConfiguration(
    val url: String,
    val allowedMethods: List<String> = listOf("GET"),
    val sslConfiguration: SslConfiguration? = null
)

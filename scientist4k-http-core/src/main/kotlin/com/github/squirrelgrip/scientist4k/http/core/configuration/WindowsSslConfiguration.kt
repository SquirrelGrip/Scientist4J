package com.github.squirrelgrip.scientist4k.http.core.configuration

class WindowsSslConfiguration: SslConfiguration(
    keyStoreType = "Windows-MY",
    trustStoreType = "Windows-ROOT"
)
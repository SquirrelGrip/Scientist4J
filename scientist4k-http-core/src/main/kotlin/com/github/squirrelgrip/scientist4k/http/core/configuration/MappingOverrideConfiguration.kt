package com.github.squirrelgrip.scientist4k.http.core.configuration

data class MappingOverrideConfiguration(
    val requestHeaders: Map<String, String> = emptyMap(),
    val responseHeaders: Map<String, String> = emptyMap(),
)

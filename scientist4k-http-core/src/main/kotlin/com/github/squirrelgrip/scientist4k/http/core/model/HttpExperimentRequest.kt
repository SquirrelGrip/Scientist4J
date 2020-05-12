package com.github.squirrelgrip.scientist4k.http.core.model

data class HttpExperimentRequest(
        val method: String,
        val url: String,
        val headers: Map<String, String> = emptyMap(),
        val parameters: Map<String, Array<String>> = emptyMap(),
        val body: String = ""
)

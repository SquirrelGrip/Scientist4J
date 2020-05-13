package com.github.squirrelgrip.scientist4k.http.core.model

data class HttpExperimentResponse(
        val status: Int,
        val headers: Map<String, String>,
        val contentType: String,
        val contents: String? = null
)

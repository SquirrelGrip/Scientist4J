package com.github.squirrelgrip.scientist4k.http.core.model

import com.google.common.net.HttpHeaders

data class HttpExperimentResponse(
        val status: Int,
        val headers: Map<String, String>,
        val contents: String? = null
) {
    fun contentType(): String =
            headers[HttpHeaders.CONTENT_TYPE] ?: ""
}
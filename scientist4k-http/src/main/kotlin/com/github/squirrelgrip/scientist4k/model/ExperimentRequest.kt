package com.github.squirrelgrip.scientist4k.model

import javax.servlet.http.HttpServletRequest

/**
 * TODO Handle Headers with multiple values
 * TODO Handle Cookies
 */
data class ExperimentRequest(
        val method: String,
        val url: String,
        val protocol: String,
        val headers: Map<String, String> = emptyMap(),
        val contentType: String?,
        val body: ByteArray = ByteArray(0)
) {
    companion object {
        fun create(inboundRequest: HttpServletRequest): ExperimentRequest {
            val headers = inboundRequest.headerNames.toList().map {
                it to inboundRequest.getHeader(it)
            }.toMap()
            return ExperimentRequest(
                    inboundRequest.method,
                    getUrl(inboundRequest),
                    inboundRequest.protocol,
                    headers,
                    inboundRequest.contentType,
                    inboundRequest.inputStream.readBytes()
            )
        }

        private fun getUrl(inboundRequest: HttpServletRequest): String {
            val url = StringBuffer("${inboundRequest.requestURI}")
            if (!inboundRequest.queryString.isNullOrEmpty()) {
                url.append("?${inboundRequest.queryString}")
            }
            return url.toString()
        }

    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExperimentRequest

        if (method != other.method) return false
        if (url != other.url) return false
        if (protocol != other.protocol) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + protocol.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }
}

package com.github.squirrelgrip.scientist4k.http.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.StatusLine

data class ExperimentResponse(
        @JsonProperty("status")
        val status: Int,
        @JsonProperty("headers")
        val headers: Map<String, String>,
        @JsonIgnore
        val body: ByteArray
) {
    constructor(
            status: StatusLine,
            headers: Array<Header>,
            entity: HttpEntity
    ) : this(
            status.statusCode,
            headers.map { it.name to it.value }.toMap(),
            entity.content?.readBytes() ?: ByteArray(0)
    )

    val mediaType: MediaType?
        get() {
            return if (contentType != null) {
                MediaType.parse(contentType)
            } else {
                null
            }
        }

    @JsonProperty("contentType")
    val contentType: String? = this.headers[HttpHeaders.CONTENT_TYPE]

    @JsonProperty("contents")
    val contents: Any =
        if (mediaType?.isTextLike() == true) {
            String(body)
        } else {
            body
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExperimentResponse

        if (status != other.status) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

}

val textSubType = listOf("json", "geo+json", "hal+json", "xhtml", "xml", "html", "xhtml+xml", "x-www-form-urlencoded")

fun MediaType.isTextLike(): Boolean {
    return this.`is`(MediaType.ANY_TEXT_TYPE)
            || this.parameters().containsKey("charset")
            || (this.`is`(MediaType.ANY_APPLICATION_TYPE) && textSubType.contains(this.subtype()))
}

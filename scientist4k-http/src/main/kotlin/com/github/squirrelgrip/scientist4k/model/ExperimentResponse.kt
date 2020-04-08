package com.github.squirrelgrip.scientist4k.model

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.StatusLine

data class ExperimentResponse(
        val status: StatusLine,
        val headers: Array<Header>,
        val entity: HttpEntity) {

    val content: ByteArray = entity.content?.readBytes() ?: ByteArray(0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExperimentResponse

        if (status != other.status) return false
        if (!headers.contentEquals(other.headers)) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + headers.contentHashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }

}

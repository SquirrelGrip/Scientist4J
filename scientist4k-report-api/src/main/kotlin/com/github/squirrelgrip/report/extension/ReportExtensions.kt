package com.github.squirrelgrip.report.extension

import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import java.security.MessageDigest
import java.util.*

fun String.toHash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    val bytes = messageDigest.digest()
    return bytes.toBase64()
}

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHexString(): String {
    val hexChars = CharArray(this.size * 2)
    for (j in this.indices) {
        val v: Int = this[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

fun ByteArray.toBase64() : String {
    return Base64.getEncoder().encodeToString(this)
}

fun HttpExperimentResult.toExperimentResultId(): String {
    return "${this.experiment}:${this.request.method}:${this.request.url}:${this.id}".toHash()
}

fun HttpExperimentResult.toExperimentUrlId(): String {
    return "${this.experiment}:${this.request.method}:${this.request.url}".toHash()
}

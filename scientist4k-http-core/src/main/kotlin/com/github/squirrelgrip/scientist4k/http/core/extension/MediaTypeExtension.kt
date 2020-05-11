package com.github.squirrelgrip.scientist4k.http.core.extension

import com.google.common.net.MediaType

val textSubType = listOf("json", "geo+json", "hal+json", "xhtml", "xml", "html", "xhtml+xml", "x-www-form-urlencoded")

fun MediaType.isTextLike(): Boolean {
    return this.`is`(MediaType.ANY_TEXT_TYPE)
            || this.parameters().containsKey("charset")
            || (this.`is`(MediaType.ANY_APPLICATION_TYPE) && textSubType.contains(this.subtype()))
}
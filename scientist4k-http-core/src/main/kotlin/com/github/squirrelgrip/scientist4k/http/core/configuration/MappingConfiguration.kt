package com.github.squirrelgrip.scientist4k.http.core.configuration

import java.util.regex.Pattern

data class MappingConfiguration(
        val control: String,
        val candidate: String
) {
    val controlPattern: Pattern = Pattern.compile(control)

    fun matches(uri: String): Boolean {
        return controlPattern.matcher(uri).matches()
    }

    fun replace(uri: String): String {
        val matcher = controlPattern.matcher(uri)
        val stringBuffer = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, candidate)
        }
        matcher.appendTail(stringBuffer)
        return stringBuffer.toString()
    }


}

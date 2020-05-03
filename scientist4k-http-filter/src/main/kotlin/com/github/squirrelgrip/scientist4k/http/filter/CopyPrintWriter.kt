package com.github.squirrelgrip.scientist4k.http.filter

import java.io.PrintWriter
import java.io.Writer

class CopyPrintWriter(
        writer: Writer
) : PrintWriter(writer) {
    private val copy = StringBuilder()

    override fun write(c: Int) {
        copy.append(c.toChar())
        super.write(c)
    }

    override fun write(chars: CharArray, offset: Int, length: Int) {
        copy.append(chars, offset, length)
        super.write(chars, offset, length)
    }

    override fun write(string: String, offset: Int, length: Int) {
        copy.append(string, offset, length)
        super.write(string, offset, length)
    }

    override fun println() {
        copy.append("\n")
        super.println()
    }

    fun getCopy(): ByteArray {
        val string = copy.toString()
        return string.toByteArray()
    }
}
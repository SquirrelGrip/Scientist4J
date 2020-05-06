package com.github.squirrelgrip.scientist4k.http.core.wrapper

import java.io.ByteArrayOutputStream
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener

class CopyServletOutputStream(
        val outputStream: ServletOutputStream
) : ServletOutputStream() {
    private val copy: ByteArrayOutputStream = ByteArrayOutputStream(1024)

    override fun isReady(): Boolean {
        return outputStream.isReady
    }

    override fun write(b: Int) {
        outputStream.write(b)
        copy.write(b)
    }

    override fun setWriteListener(writeListener: WriteListener) {
        outputStream.setWriteListener(writeListener)
    }

    fun getCopy(): ByteArray {
        return copy.toByteArray()
    }
}


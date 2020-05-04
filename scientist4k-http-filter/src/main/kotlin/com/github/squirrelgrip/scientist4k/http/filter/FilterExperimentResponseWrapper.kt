package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import org.apache.http.Header
import org.apache.http.message.BasicHeader
import java.io.PrintWriter
import javax.servlet.ServletOutputStream
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class FilterExperimentResponseWrapper(
        response: ServletResponse
) : HttpServletResponseWrapper(response as HttpServletResponse) {
    private lateinit var copyServletOutputStream: CopyServletOutputStream
    private lateinit var copyPrintWriter: CopyPrintWriter

    override fun getOutputStream(): ServletOutputStream {
        if (!this::copyServletOutputStream.isInitialized) {
            copyServletOutputStream = CopyServletOutputStream(response.outputStream)
        }
        return copyServletOutputStream
    }

    override fun getWriter(): PrintWriter {
        if (!this::copyPrintWriter.isInitialized) {
            copyPrintWriter = CopyPrintWriter(response.writer)
        }
        return copyPrintWriter
    }

    val experimentResponse: ExperimentResponse
        get() {
            val headers = headerNames.map {
                BasicHeader(it, getHeader(it))
            }.toTypedArray<Header>()
            val content: ByteArray = when {
                this::copyServletOutputStream.isInitialized -> copyServletOutputStream.getCopy()
                this::copyPrintWriter.isInitialized -> copyPrintWriter.getCopy()
                else -> ByteArray(0)
            }
            return ExperimentResponse(status, headers, content)
        }
}
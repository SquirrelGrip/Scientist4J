package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import org.apache.http.Header
import org.apache.http.HttpResponse
import org.apache.http.StatusLine

class HttpResponseComparator(
        val debug: Boolean = false
) : ExperimentComparator<HttpResponse> {
    private val statusLineComparator = StatusLineComparator()
    private val headersComparator = HeadersComparator()

    override fun invoke(control: HttpResponse?, candidate: HttpResponse?): Boolean {
        if (control != null && candidate != null) {
            return statusLineComparator.invoke(control.statusLine, candidate.statusLine)
                    && headersComparator.invoke(control.allHeaders, candidate.allHeaders)
        }
        return control == null && candidate == null
    }
}

class StatusLineComparator : ExperimentComparator<StatusLine> {
    override fun invoke(control: StatusLine?, candidate: StatusLine?): Boolean {
        if (control != null && candidate != null) {
            return control.statusCode == candidate.statusCode
                    && control.protocolVersion.isComparable(candidate.protocolVersion)
        }
        return control == null && candidate == null
    }
}

class HeaderComparator : ExperimentComparator<Header> {
    override fun invoke(control: Header?, candidate: Header?): Boolean {
        if (control != null && candidate != null) {
            return control.value == candidate.value
        }
        return control == null && candidate == null
    }
}

class HeadersComparator : ExperimentComparator<Array<Header>> {
    private val headerComparator = HeaderComparator()
    override fun invoke(control: Array<Header>?, candidate: Array<Header>?): Boolean {
        if (control != null && candidate != null) {
            val controlMap = control.map {
                it.name to it
            }.toMap()
            val candidateMap = candidate.map {
                it.name to it
            }.toMap()
            return controlMap.filter { (key: String, header: Header) ->
                !headerComparator.invoke(header, candidateMap[key])
            }.isEmpty() && candidateMap.filter { (key: String, header: Header) ->
                !headerComparator.invoke(controlMap[key], header)
            }.isEmpty()
        }
        return control == null && candidate == null
    }
}


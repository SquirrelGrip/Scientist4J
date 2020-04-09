package com.github.squirrelgrip.scientist4k.model

import org.apache.http.Header
import org.apache.http.StatusLine

class ExperimentResponseComparator(
        val debug: Boolean = false
) : ExperimentComparator<ExperimentResponse> {
    private val statusLineComparator = StatusLineComparator()
    private val headersComparator = HeadersComparator()

    override fun invoke(control: ExperimentResponse?, candidate: ExperimentResponse?): Boolean {
        if (control != null && candidate != null) {
            val statusLineMatch = statusLineComparator.invoke(control.status, candidate.status)
            val headerMatch = headersComparator.invoke(control.headers, candidate.headers)
            return statusLineMatch && headerMatch
        }
        return control == null && candidate == null
    }
}

class StatusLineComparator : ExperimentComparator<StatusLine> {
    override fun invoke(control: StatusLine?, candidate: StatusLine?): Boolean {
        if (control != null && candidate != null) {
            val statusCodeMatch = control.statusCode == candidate.statusCode
            val protocolMatch = control.protocolVersion.isComparable(candidate.protocolVersion)
            return statusCodeMatch && protocolMatch
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
            val controlMap = control.filter {
                it.name != "Set-Cookie"
            }.map {
                it.name to it
            }.toMap()
            val candidateMap = candidate.filter {
                it.name != "Set-Cookie"
            }.map {
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


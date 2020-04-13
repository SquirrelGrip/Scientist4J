package com.github.squirrelgrip.scientist4k.model

import com.github.squirrelgrip.scientist4k.model.ComparisonResult.Companion.SUCCESS
import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import com.google.common.net.HttpHeaders.*
import org.apache.http.Header
import org.apache.http.ProtocolVersion
import org.apache.http.StatusLine

class ExperimentResponseComparator(
        val debug: Boolean = false
) : ExperimentComparator<ExperimentResponse?> {
    private val statusLineComparator = StatusLineComparator()
    private val headersComparator = HeadersComparator()
    private val contentComparator = ContentComparator()

    override fun invoke(control: ExperimentResponse?, candidate: ExperimentResponse?): ComparisonResult {
        if (control != null && candidate != null) {
            val statusLineMatch = statusLineComparator.invoke(control.status, candidate.status)
            val headerMatch = headersComparator.invoke(control.headers, candidate.headers)
            val contentMatch = contentComparator.invoke(control.content, candidate.content)
            return ComparisonResult(statusLineMatch, headerMatch, contentMatch)
        }
        return ComparisonResult("Either Control or Candidate responses is null.")
    }
}

class ContentComparator : ExperimentComparator<ByteArray> {
    override fun invoke(control: ByteArray, candidate: ByteArray): ComparisonResult {
        if (control.contentEquals(candidate)) {
            return SUCCESS
        }
        return ComparisonResult("Contents different.")
    }
}

class StatusLineComparator : ExperimentComparator<StatusLine> {
    private val statusComparator = StatusComparator()
    private val protocolComparator = ProtocolComparator()
    override fun invoke(control: StatusLine, candidate: StatusLine): ComparisonResult {
        val statusCodeMatch = statusComparator.invoke(control.statusCode, candidate.statusCode)
        val protocolMatch = protocolComparator.invoke(control.protocolVersion, candidate.protocolVersion)
        return ComparisonResult(statusCodeMatch, protocolMatch)
    }
}

class StatusComparator : ExperimentComparator<Int> {
    override fun invoke(control: Int, candidate: Int): ComparisonResult {
        if (control == candidate) {
            return SUCCESS
        }
        return ComparisonResult("Control returned status $control and Candidate returned status $candidate.")
    }
}

class ProtocolComparator : ExperimentComparator<ProtocolVersion> {
    override fun invoke(control: ProtocolVersion, candidate: ProtocolVersion): ComparisonResult {
        if (control == candidate) {
            return SUCCESS
        }
        return ComparisonResult("Control protocol $control and Candidate protocol $candidate did not match.")
    }
}

class HeadersComparator : ExperimentComparator<Array<Header>> {
    override fun invoke(control: Array<Header>, candidate: Array<Header>): ComparisonResult {
        val controlMap = map(control)
        val candidateMap = map(candidate)

        val diff: MapDifference<String, String> = Maps.difference(controlMap, candidateMap)
        if (diff.areEqual()) {
            return SUCCESS
        }
        val entriesDiffering = diff.entriesDiffering().map { (headerName, headerValue) ->
            "Header[$headerName] value is different: ${headerValue.leftValue()} != ${headerValue.rightValue()}."
        }.toComparisonResult()
        val entriesOnlyInControl = diff.entriesOnlyOnLeft().keys.map {
            "Header[$it] is only in Control."
        }.toComparisonResult()
        val entriesOnlyInCandidate = diff.entriesOnlyOnRight().keys.map {
            "Header[$it] is only in Candidate."
        }.toComparisonResult()
        return ComparisonResult(entriesDiffering, entriesOnlyInControl, entriesOnlyInCandidate)
    }

    val IGNORED_HEADERS = arrayOf(
            SET_COOKIE,
            LAST_MODIFIED,
            DATE,
            CONTENT_LENGTH
    )

    private fun map(control: Array<Header>): Map<String, String> {
        return (control).filter {
            it.name !in IGNORED_HEADERS
        }.map {
            it.name to it.value
        }.toMap<String, String>()
    }
}

private fun List<String>.toComparisonResult(): ComparisonResult = ComparisonResult(this)


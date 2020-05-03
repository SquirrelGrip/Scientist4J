package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult.Companion.SUCCESS
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import com.google.common.net.HttpHeaders.*
import org.apache.http.Header

class HeadersComparator : ExperimentComparator<ExperimentResponse> {
    override fun invoke(control: ExperimentResponse, candidate: ExperimentResponse): ComparisonResult {
        val controlMap = map(control.headers)
        val candidateMap = map(candidate.headers)

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
            CONTENT_LENGTH,
            CONTENT_TYPE,
            EXPIRES,
            SERVER
    )

    private fun map(control: Array<Header>): Map<String, String> {
        return (control).filter {
            it.name !in IGNORED_HEADERS
        }.map {
            it.name to it.value
        }.toMap<String, String>()
    }
}

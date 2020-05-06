package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult.Companion.SUCCESS
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.toComparisonResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class HeadersComparator(
        vararg val ignoredHeaders: String
) : ExperimentComparator<ExperimentResponse> {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(HeadersComparator::class.java)
    }

    override fun invoke(control: ExperimentResponse, candidate: ExperimentResponse): ComparisonResult {
        LOGGER.trace("Comparing Headers...")
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

    private fun map(control: Map<String, String>): Map<String, String> {
        return control.filter {
            !ignoredHeaders.contains(it.key)
        }
    }
}

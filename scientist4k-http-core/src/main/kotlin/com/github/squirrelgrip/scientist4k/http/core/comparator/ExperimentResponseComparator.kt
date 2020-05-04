package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult.Companion.SUCCESS
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExperimentResponseComparator() : ExperimentComparator<ExperimentResponse?> {
    private val statusComparator = StatusComparator()
    private val headersComparator = HeadersComparator()
    private val contentComparator = ContentComparator()

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ExperimentComparator::class.java)
    }

    override fun invoke(control: ExperimentResponse?, candidate: ExperimentResponse?): ComparisonResult {
        if (control != null && candidate != null) {
            LOGGER.info("Comparing StatusLine...")
            val statusMatch = statusComparator.invoke(control.status, candidate.status)
            LOGGER.info("Comparing Headers...")
            val headerMatch = headersComparator.invoke(control, candidate)
            LOGGER.info("Comparing Contents...")
            val contentMatch = contentComparator.invoke(control, candidate)
            return ComparisonResult(statusMatch, headerMatch, contentMatch)
        }
        return ComparisonResult("Either Control or Candidate responses is null.")
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

fun List<String>.toComparisonResult(): ComparisonResult = ComparisonResult(this)


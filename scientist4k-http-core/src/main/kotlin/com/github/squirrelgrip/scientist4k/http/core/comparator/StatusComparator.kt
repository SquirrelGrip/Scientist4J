package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult.Companion.SUCCESS
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StatusComparator : ExperimentComparator<HttpExperimentResponse> {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(StatusComparator::class.java)
    }

    override fun invoke(control: HttpExperimentResponse, candidate: HttpExperimentResponse): ComparisonResult {
        LOGGER.trace("Comparing StatusLine...")
        if (control.status == candidate.status) {
            return SUCCESS
        }
        return ComparisonResult("Control returned status ${control.status} and Candidate returned status ${candidate.status}.")
    }
}
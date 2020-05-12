package com.github.squirrelgrip.scientist4k.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultExperimentComparator<T> : ExperimentComparator<T?> {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultExperimentComparator::class.java)
    }

    override fun invoke(control: T?, candidate: T?): ComparisonResult {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Control: $control")
            LOGGER.debug("Candidate: $candidate")
        }
        return when {
            control != candidate -> ComparisonResult("Control value does not match the Candidate value.")
            else -> ComparisonResult.SUCCESS
        }
    }
}

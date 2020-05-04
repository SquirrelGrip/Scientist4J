package com.github.squirrelgrip.scientist4k.core.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface ExperimentComparator<T> : (T, T) -> ComparisonResult

data class ComparisonResult(
        val failureReasons: List<String>
) {
    constructor(vararg failureReasons: String): this(failureReasons.toList())
    constructor(vararg comparisonResult: ComparisonResult): this(comparisonResult.flatMap { it.failureReasons })

    companion object {
        val SUCCESS = ComparisonResult(emptyList())
    }
    val matches: Boolean by lazy {
        failureReasons.isEmpty()
    }
}

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


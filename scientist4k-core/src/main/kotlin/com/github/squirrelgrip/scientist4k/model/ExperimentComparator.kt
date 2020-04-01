package com.github.squirrelgrip.scientist4k.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface ExperimentComparator<T> : (T?, T?) -> Boolean

class DefaultExperimentComparator<T> : ExperimentComparator<T> {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultExperimentComparator::class.java)
    }

    override fun invoke(control: T?, candidate: T?): Boolean {
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Control: $control")
            LOGGER.debug("Candidate: $candidate")
        }
        return control == candidate
    }
}
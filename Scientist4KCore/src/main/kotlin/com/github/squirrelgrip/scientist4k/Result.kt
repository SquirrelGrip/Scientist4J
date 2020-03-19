package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.exceptions.MismatchException

class Result<T>(
        private val experiment: Experiment<T>,
        val control: Observation<T>,
        val candidate: Observation<T>?,
        val context: Map<String, Any> = emptyMap()) {
    val match: Boolean = if (candidate != null) {
        experiment.compare(control, candidate)
    } else {
        false
    }

    fun handleComparisonMismatch() {
        if (experiment.raiseOnMismatch && candidate != null && !match) {
            val exception = candidate.exception
            val msg = if (exception != null) {
                val stackTrace = exception.stackTrace.toString()
                val exceptionName = exception.javaClass.name
                "${candidate.name} raised an exception: $exceptionName $stackTrace"
            } else {
                "${candidate.name} does not match control value (${control.value} != ${candidate.value})"
            }
            throw MismatchException(msg)
        }
    }

}
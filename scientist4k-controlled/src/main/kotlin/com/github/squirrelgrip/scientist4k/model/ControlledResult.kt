package com.github.squirrelgrip.scientist4k.model

import com.github.squirrelgrip.scientist4k.ControlledExperiment
import com.github.squirrelgrip.scientist4k.exception.MismatchException
import com.github.squirrelgrip.scientist4k.model.sample.Sample

class ControlledResult<T>(
        private val experiment: ControlledExperiment<T>,
        val control: Observation<T>,
        val reference: Observation<T>,
        val candidate: Observation<T>,
        val sample: Sample
) {
    val match =
            experiment.compare(control, candidate)

    fun handleComparisonMismatch() {
        if (experiment.raiseOnMismatch && !match.matches) {
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
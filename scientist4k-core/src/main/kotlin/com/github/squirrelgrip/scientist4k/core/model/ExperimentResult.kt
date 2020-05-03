package com.github.squirrelgrip.scientist4k.core.model

import com.github.squirrelgrip.scientist4k.core.Experiment
import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample

class ExperimentResult<T>(
        private val experiment: Experiment<T>,
        val control: Observation<T>,
        val candidate: Observation<T>?,
        val sample: Sample
) {
    val match =
        if (candidate == null) {
            ComparisonResult((listOf("Candidate observation is null")))
        } else {
            experiment.compare(control, candidate)
        }

    fun handleComparisonMismatch() {
        if (experiment.raiseOnMismatch && candidate != null && !match.matches) {
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

    override fun toString(): String {
        return "${experiment.name} ($match)\n\tControl  => $control\n\tCandidate=> $candidate"
    }

}
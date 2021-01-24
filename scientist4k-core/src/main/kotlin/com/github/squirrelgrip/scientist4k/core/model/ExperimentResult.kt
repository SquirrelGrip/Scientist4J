package com.github.squirrelgrip.scientist4k.core.model

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample

open class ExperimentResult<T>(
        private val simpleExperiment: AbstractExperiment<T>,
        val control: ExperimentObservation<T>,
        val candidate: ExperimentObservation<T>?,
        val sample: Sample
) {
    val match =
        if (candidate == null) {
            ComparisonResult((listOf("Candidate observation is null")))
        } else {
            simpleExperiment.compare(control, candidate)
        }

    fun handleComparisonMismatch() {
        if (simpleExperiment.experimentOptions.contains(ExperimentOption.RAISE_ON_MISMATCH) && candidate != null && !match.matches) {
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
        return "${simpleExperiment.name} ($match)\n\tControl  => $control\n\tCandidate=> $candidate"
    }

}
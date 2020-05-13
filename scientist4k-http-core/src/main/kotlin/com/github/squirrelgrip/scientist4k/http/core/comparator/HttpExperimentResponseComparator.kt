package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResponse

open class HttpExperimentResponseComparator(vararg val comparators: ExperimentComparator<HttpExperimentResponse>) : ExperimentComparator<HttpExperimentResponse?> {
    override fun invoke(control: HttpExperimentResponse?, candidate: HttpExperimentResponse?): ComparisonResult {
        if (control != null && candidate != null) {
            val list = comparators.map { it.invoke(control, candidate) }
            return ComparisonResult(*list.toTypedArray())
        }
        return ComparisonResult("Either Control or Candidate responses is null.")
    }
}



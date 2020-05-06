package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse

open class ExperimentResponseComparator(vararg val comparators: ExperimentComparator<ExperimentResponse>) : ExperimentComparator<ExperimentResponse?> {
    override fun invoke(control: ExperimentResponse?, candidate: ExperimentResponse?): ComparisonResult {
        if (control != null && candidate != null) {
            val list = comparators.map { it.invoke(control, candidate) }
            return ComparisonResult(*list.toTypedArray())
        }
        return ComparisonResult("Either Control or Candidate responses is null.")
    }
}



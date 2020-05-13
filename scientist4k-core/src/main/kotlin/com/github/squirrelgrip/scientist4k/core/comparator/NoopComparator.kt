package com.github.squirrelgrip.scientist4k.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult

class NoopComparator<T>: ExperimentComparator<T?> {
    override fun invoke(p1: T?, p2: T?): ComparisonResult {
        return ComparisonResult.SUCCESS
    }
}
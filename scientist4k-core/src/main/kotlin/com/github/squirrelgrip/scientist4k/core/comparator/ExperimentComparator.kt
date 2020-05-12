package com.github.squirrelgrip.scientist4k.core.comparator

import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult

interface ExperimentComparator<T> : (T, T) -> ComparisonResult

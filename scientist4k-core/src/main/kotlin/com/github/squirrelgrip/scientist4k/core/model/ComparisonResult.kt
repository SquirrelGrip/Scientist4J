package com.github.squirrelgrip.scientist4k.core.model

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

fun List<String>.toComparisonResult(): ComparisonResult = ComparisonResult(this)

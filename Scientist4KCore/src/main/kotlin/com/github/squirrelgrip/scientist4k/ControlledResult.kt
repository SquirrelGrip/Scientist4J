package com.github.squirrelgrip.scientist4k

class ControlledResult<T>(
        val sample: Sample,
        val controlResult: Result<T>,
        val candidateResult: Result<T>
) {
    val match = candidateResult.match == controlResult.match

}
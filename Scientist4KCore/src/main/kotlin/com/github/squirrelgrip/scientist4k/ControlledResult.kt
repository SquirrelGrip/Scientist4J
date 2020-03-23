package com.github.squirrelgrip.scientist4k

class ControlledResult<T>(
        val note: Note,
        val controlResult: Result<T>,
        val candidateResult: Result<T>
) {
    val match = candidateResult.match == controlResult.match

}
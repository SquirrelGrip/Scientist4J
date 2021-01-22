package com.github.squirrelgrip.scientist4k.core.model

import java.util.*

enum class ExperimentOption {
    ENABLED,
    ASYNC,
    RETURN_CANDIDATE,
    RAISE_ON_MISMATCH;

    companion object {
        val DEFAULT: EnumSet<ExperimentOption> =
            EnumSet.of(ENABLED, ASYNC)

        val MISMATCHED_EXCEPTION: EnumSet<ExperimentOption> =
            EnumSet.of(ENABLED, ASYNC, RAISE_ON_MISMATCH)
    }
}
package com.github.squirrelgrip.scientist4k.core.model

import java.util.*

enum class ExperimentFlag {
    DISABLED,
    SYNC,
    RETURN_CANDIDATE,
    RAISE_ON_MISMATCH;

    companion object {
        val DEFAULT: EnumSet<ExperimentFlag> =
            EnumSet.noneOf(ExperimentFlag::class.java)
    }
}
package com.github.squirrelgrip.scientist4k.core.model

import java.util.*

enum class ExperimentOption {
    DISABLED,
    SYNC,
    RETURN_CANDIDATE,
    RETURN_REFERENCE,
    RAISE_ON_MISMATCH,
    WITHHOLD_PUBLICATION;

    companion object {
        val DEFAULT: EnumSet<ExperimentOption> =
            EnumSet.noneOf(ExperimentOption::class.java)

    }
}
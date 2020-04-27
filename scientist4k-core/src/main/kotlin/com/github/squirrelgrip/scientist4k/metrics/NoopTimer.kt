package com.github.squirrelgrip.scientist4k.metrics

class NoopTimer : Timer {
    override fun record(runnable: () -> Unit) {
        // Do Nothing
    }

    override val duration: Long = 0
}
package com.github.squirrelgrip.scientist4k.metrics.noop

import com.github.squirrelgrip.scientist4k.metrics.Timer

class NoopTimer : Timer {
    override fun record(runnable: () -> Unit) {
        runnable.invoke()
    }

    override val duration: Long = 0
}
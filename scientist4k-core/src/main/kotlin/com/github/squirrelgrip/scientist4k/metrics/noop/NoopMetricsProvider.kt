package com.github.squirrelgrip.scientist4k.metrics.noop

import com.github.squirrelgrip.scientist4k.metrics.Counter
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.Timer

/**
 * A  minimal in-memory [MetricsProvider] implementation, suitable for test environments or if no metrics required.
 */
class NoopMetricsProvider : MetricsProvider<Any> {
    override fun timer(vararg nameComponents: String): Timer {
        return object : Timer {
            override val duration: Long = 0
            override fun record(runnable: () -> Unit) {
                runnable.invoke()
            }
        }
    }

    override fun counter(vararg nameComponents: String): Counter {
        return object : Counter {
            override fun increment() {
            }
        }
    }

    override val registry: Any = Any()
}
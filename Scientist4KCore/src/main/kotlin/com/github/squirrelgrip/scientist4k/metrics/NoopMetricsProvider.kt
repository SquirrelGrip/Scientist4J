package com.github.squirrelgrip.scientist4k.metrics

/**
 * A  minimal in-memory [MetricsProvider] implementation, suitable for test environments or if no metrics required.
 */
class NoopMetricsProvider : MetricsProvider<Any> {
    override fun timer(vararg nameComponents: String): Timer {
        return object : Timer {
            override var duration: Long = 0
            override fun record(runnable: () -> Unit) {
                val now = System.nanoTime()
                runnable.invoke()
                duration = System.nanoTime() - now
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
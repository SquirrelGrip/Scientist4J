package com.github.squirrelgrip.scientist4k.metrics

import io.dropwizard.metrics5.MetricRegistry

class DropwizardMetricsProvider(
        override val registry: MetricRegistry = MetricRegistry()
) : MetricsProvider<MetricRegistry> {
    override fun timer(vararg nameComponents: String): Timer {
        return object : Timer {
            private var durationVariable: Long = 0
            val timer = registry.timer(MetricRegistry.name(nameComponents[0], *nameComponents.copyOfRange(1, nameComponents.size)))
            override val duration: Long
                get() = durationVariable

            override fun record(runnable: () -> Unit) {
                val context = timer.time()
                try {
                    runnable.invoke()
                } finally {
                    durationVariable = context.stop()
                }
            }

        }
    }

    override fun counter(vararg nameComponents: String): Counter {
        return object : Counter {
            val counter = registry.counter(MetricRegistry.name(nameComponents[0], *nameComponents.copyOfRange(1, nameComponents.size)))
            override fun increment() {
                counter.inc()
            }
        }
    }

}
package com.github.squirrelgrip.scientist4k.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.concurrent.TimeUnit

class MicrometerMetricsProvider(
        override var registry: MeterRegistry = SimpleMeterRegistry()
) : MetricsProvider<MeterRegistry> {
    override fun timer(vararg nameComponents: String): Timer {
        return object : Timer {
            val timer = io.micrometer.core.instrument.Timer.builder(nameComponents.joinToString(separator = ".")).register(registry)
            override fun record(runnable: () -> Unit) {
                timer.record(runnable)
            }

            override val duration: Long
                get() = timer.totalTime(TimeUnit.NANOSECONDS).toLong()
        }
    }

    override fun counter(vararg nameComponents: String): Counter {
        return object : Counter {
            val counter = io.micrometer.core.instrument.Counter.builder(nameComponents.joinToString(separator = ".")).register(registry)
            override fun increment() {
                counter.increment()
            }
        }
    }

}
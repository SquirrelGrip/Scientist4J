package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import org.assertj.core.api.Assertions

class TestPublishExperiment<Integer>(name: String, metricsProvider: MetricsProvider<*>) : Experiment<Integer>(name, metricsProvider) {
    override fun publish(r: Result<Integer>) {
        Assertions.assertThat(r.candidate?.duration).isGreaterThan(0L)
        Assertions.assertThat(r.control.duration).isGreaterThan(0L)
    }
}
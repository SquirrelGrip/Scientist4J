package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.Result
import org.assertj.core.api.Assertions

class TestPublishExperiment<Integer>(name: String, metricsProvider: MetricsProvider<*>) : Experiment<Integer>(name, metricsProvider) {
    override fun publish(result: Result<Integer>) {
        Assertions.assertThat(result.candidate?.duration).isGreaterThan(0L)
        Assertions.assertThat(result.control.duration).isGreaterThan(0L)
    }
}

class TestPublishControlledExperiment<Integer>(name: String, metricsProvider: MetricsProvider<*>) : ControlledExperiment<Integer>(name, metricsProvider) {
    override fun publish(result: Result<Integer>) {
        Assertions.assertThat(result.candidate?.duration).isGreaterThan(0L)
        Assertions.assertThat(result.control.duration).isGreaterThan(0L)
    }
}
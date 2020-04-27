package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.ControlledResult
import org.assertj.core.api.Assertions

class TestPublishControlledExperiment<Integer>(name: String, metricsProvider: MetricsProvider<*>) : ControlledExperiment<Integer>(name, metricsProvider) {
    override fun publish(result: ControlledResult<Integer>) {
        Assertions.assertThat(result.candidate?.duration).isGreaterThan(0L)
        Assertions.assertThat(result.reference.duration).isGreaterThan(0L)
        Assertions.assertThat(result.control.duration).isGreaterThan(0L)
    }
}
package com.github.squirrelgrip.scientist4k.model

import com.github.squirrelgrip.extensions.json.toJson
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ObservationTest {
    @Test
    fun `Observation set value to null`() {
        val observation = Observation<Int>("observation", NoopMetricsProvider().timer())
        assertThat(observation.toJson()).isEqualTo("{\"name\":\"observation\",\"value\":null,\"exception\":null,\"duration\":0}")
    }

    @Test
    fun `Observation set value`() {
        val observation = Observation<Int>("observation", NoopMetricsProvider().timer())
        observation.setValue(1)
        assertThat(observation.toJson()).isEqualTo("{\"name\":\"observation\",\"value\":1,\"exception\":null,\"duration\":0}")
    }
}

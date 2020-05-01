package com.github.squirrelgrip.scientist4k.model

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ObservationTest {
    @Test
    fun `Observation set value to null`() {
        val observation = Observation<Int>("observation", NoopMetricsProvider().timer())
        observation.time { null }
        assertThat(observation.toJson()).isEqualTo("""{"name":"observation","value":null,"exception":null,"duration":0,"status":"COMPLETED"}""")
        assertThat(observation.toString()).isEqualTo("null")
    }

    @Test
    fun `Observation set exception`() {
        val observation = Observation<Int>("observation", NoopMetricsProvider().timer())
        observation.time { throw Exception("Message") }
        assertThat(observation.toJson()).isEqualTo("""{"name":"observation","value":null,"exception":{"cause":null,"message":"Message","localizedMessage":"Message","suppressed":[]},"duration":0,"status":"COMPLETED"}""")
        assertThat(observation.toString()).isEqualTo("Message")
    }

    @Test
    fun `Observation set value`() {
        val observation = Observation<Int>("observation", NoopMetricsProvider().timer())
        observation.setValue(1)
        assertThat(observation.toJson()).isEqualTo("""{"name":"observation","value":1,"exception":null,"duration":0,"status":"COMPLETED"}""")
        assertThat(observation.toString()).isEqualTo("1")
    }
}

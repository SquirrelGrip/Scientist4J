package com.github.squirrelgrip.scientist4k.core.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import java.io.File

data class ExperimentConfiguration(
        val name: String,
        val raiseOnMismatch: Boolean = false,
        val metricsProvider: String = "DROPWIZARD",
        val context: Map<String, Any> = emptyMap(),
        val samplePrefix: String = ""
) {
    @JsonIgnore
    val metrics: MetricsProvider<*> = MetricsProvider.build(metricsProvider)

    @JsonIgnore
    val sampleFactory: SampleFactory = SampleFactory(samplePrefix)

    companion object {
        fun loadFromFile(file: File)=
                file.toInstance<ExperimentConfiguration>()
    }
}

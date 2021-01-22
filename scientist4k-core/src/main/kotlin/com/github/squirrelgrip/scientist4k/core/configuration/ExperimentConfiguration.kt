package com.github.squirrelgrip.scientist4k.core.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import java.io.File
import java.util.*

data class ExperimentConfiguration(
    val name: String,
    val metricsProvider: String = "DROPWIZARD",
    val context: Map<String, Any> = emptyMap(),
    val samplePrefix: String = "",
    val experimentFlags: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
) {
    @JsonIgnore
    val metrics: MetricsProvider<*> = MetricsProvider.build(metricsProvider)

    @JsonIgnore
    val sampleFactory: SampleFactory = SampleFactory(samplePrefix)

    companion object {
        fun loadFromFile(file: File) = file.toInstance<ExperimentConfiguration>()
    }
}

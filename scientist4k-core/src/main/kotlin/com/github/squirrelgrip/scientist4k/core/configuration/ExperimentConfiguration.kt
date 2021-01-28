package com.github.squirrelgrip.scientist4k.core.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.metrics.Metrics
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import java.io.File
import java.util.*

data class ExperimentConfiguration(
    val name: String,
    val metrics: Metrics = Metrics.DROPWIZARD,
    val samplePrefix: String = "",
    val experimentOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT,
    val sampleThreshold: Int = 100
) {
    @JsonIgnore
    val metricsProvider: MetricsProvider<*> = metrics.getProvider()

    @JsonIgnore
    val sampleFactory: SampleFactory = SampleFactory(samplePrefix)

    companion object {
        fun loadFromFile(file: File) = file.toInstance<ExperimentConfiguration>()
    }
}

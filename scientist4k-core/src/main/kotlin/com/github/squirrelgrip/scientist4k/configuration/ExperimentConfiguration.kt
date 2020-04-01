package com.github.squirrelgrip.scientist4k.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.squirrelgrip.extensions.json.toInstance
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.model.DefaultExperimentComparator
import com.github.squirrelgrip.scientist4k.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.model.sample.SampleFactory
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

    fun saveToFile(file: File) {
        ObjectMapper().registerModule(KotlinModule()).writeValue(file, this)
    }

    companion object {
        fun loadFromFile(file: File)=
                file.toInstance(ExperimentConfiguration::class.java)
    }
}

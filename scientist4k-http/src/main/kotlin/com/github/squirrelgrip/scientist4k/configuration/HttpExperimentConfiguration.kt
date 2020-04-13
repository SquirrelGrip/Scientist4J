package com.github.squirrelgrip.scientist4k.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File

data class HttpExperimentConfiguration(
        val server: ServerConfiguration,
        val experiment: ExperimentConfiguration,
        val control: EndPointConfiguration,
        val candidate: EndPointConfiguration,
        val mappings: Map<String, String> = emptyMap()
) {
    fun saveToFile(file: File) {
        ObjectMapper().registerModule(KotlinModule()).writeValue(file, this)
    }

}

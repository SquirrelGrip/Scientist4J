package com.github.squirrelgrip.scientist4k.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.squirrelgrip.extensions.json.toInstance
import java.io.File

data class HttpExperimentConfiguration(
        val server: ServerConfiguration,
        val experiment: ExperimentConfiguration,
        val control: EndPointConfiguration,
        val candidate: EndPointConfiguration
) {
    fun saveToFile(file: File) {
        ObjectMapper().registerModule(KotlinModule()).writeValue(file, this)
    }

    companion object {
        fun loadFromFile(file: File) =
                file.toInstance<HttpExperimentConfiguration>()
    }

}

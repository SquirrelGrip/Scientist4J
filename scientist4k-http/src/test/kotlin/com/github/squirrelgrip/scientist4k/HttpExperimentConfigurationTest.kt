package com.github.squirrelgrip.scientist4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.squirrelgrip.scientist4k.configuration.HttpExperimentConfiguration
import org.junit.jupiter.api.Test
import java.io.File

class HttpExperimentConfigurationTest {
    @Test
    fun `can be deserialised`() {
        val httpExperimentConfig = ObjectMapper().registerModule(KotlinModule()).readValue(File("src/test/resources/config.json"), HttpExperimentConfiguration::class.java)
    }
}
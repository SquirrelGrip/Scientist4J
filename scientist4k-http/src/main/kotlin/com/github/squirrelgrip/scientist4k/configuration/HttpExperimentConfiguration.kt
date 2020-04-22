package com.github.squirrelgrip.scientist4k.configuration

import com.github.squirrelgrip.extensions.json.toInstance

data class HttpExperimentConfiguration(
        val server: ServerConfiguration,
        val experiment: ExperimentConfiguration,
        val control: EndPointConfiguration,
        val candidate: EndPointConfiguration,
        val mappings: Map<String, String> = emptyMap()
)

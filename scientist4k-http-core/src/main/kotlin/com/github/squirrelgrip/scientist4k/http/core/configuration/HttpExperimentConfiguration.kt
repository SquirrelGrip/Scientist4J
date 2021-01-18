package com.github.squirrelgrip.scientist4k.http.core.configuration

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration

data class HttpExperimentConfiguration(
        val server: ServerConfiguration,
        val experiment: ExperimentConfiguration,
        val control: EndPointConfiguration,
        val reference: EndPointConfiguration,
        val candidate: EndPointConfiguration,
        val mappings: Map<String, String> = emptyMap()
)

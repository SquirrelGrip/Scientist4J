package com.github.squirrelgrip.scientist4k.http.server

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration

data class HttpExperimentConfiguration(
    val server: ServerConfiguration,
    val experiment: ExperimentConfiguration,
    val control: EndPointConfiguration,
    val candidate: EndPointConfiguration,
    val mappings: MappingsConfiguration
)

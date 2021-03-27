package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration

data class FilterExperimentConfiguration(
    val experiment: ExperimentConfiguration,
    val detour: EndPointConfiguration,
    val mappings: MappingsConfiguration
)

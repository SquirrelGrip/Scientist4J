package com.github.squirrelgrip.scientist4k.controlled.http.filter

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingsConfiguration

data class ControlledFilterExperimentConfiguration(
    val experiment: ExperimentConfiguration,
    val detour: EndPointConfiguration,
    val reference: EndPointConfiguration,
    val mappings: MappingsConfiguration
)

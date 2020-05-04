package com.github.squirrelgrip.scientist4k.http.filter

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.Mode
import com.github.squirrelgrip.scientist4k.http.core.configuration.EndPointConfiguration

data class FilterExperimentConfiguration(
        val experiment: ExperimentConfiguration,
        val alternateRoute: EndPointConfiguration,
        val mappings: Map<String, String> = emptyMap(),
        val mode: Mode = Mode.NORMAL
)

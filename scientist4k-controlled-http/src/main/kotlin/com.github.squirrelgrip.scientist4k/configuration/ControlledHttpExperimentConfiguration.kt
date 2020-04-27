package com.github.squirrelgrip.scientist4k.configuration

data class ControlledHttpExperimentConfiguration(
        val server: ServerConfiguration,
        val experiment: ExperimentConfiguration,
        val control: EndPointConfiguration,
        val reference: EndPointConfiguration,
        val candidate: EndPointConfiguration,
        val mappings: Map<String, String> = emptyMap()
)

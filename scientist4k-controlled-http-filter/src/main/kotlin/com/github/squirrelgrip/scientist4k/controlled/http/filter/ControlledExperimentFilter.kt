package com.github.squirrelgrip.scientist4k.controlled.http.filter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class ControlledExperimentFilter(
        val experiment: ControlledFilterExperiment
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        experiment.run(request, response, chain)
    }

}
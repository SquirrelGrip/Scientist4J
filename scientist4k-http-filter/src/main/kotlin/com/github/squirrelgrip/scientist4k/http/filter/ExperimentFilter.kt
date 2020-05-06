package com.github.squirrelgrip.scientist4k.http.filter

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class ExperimentFilter(
        val experiment: FilterExperiment
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        experiment.run(request, response, chain)
    }

}
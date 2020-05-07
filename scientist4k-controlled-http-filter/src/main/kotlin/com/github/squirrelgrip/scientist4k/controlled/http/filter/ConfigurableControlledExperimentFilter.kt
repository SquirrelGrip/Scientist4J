package com.github.squirrelgrip.scientist4k.controlled.http.filter

import com.github.squirrelgrip.extension.json.toInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import javax.servlet.*

class ConfigurableControlledExperimentFilter : Filter {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ConfigurableControlledExperimentFilter::class.java)
    }

    lateinit var experiment: ControlledFilterExperiment

    override fun init(filterConfig: FilterConfig) {
        val config = filterConfig.getInitParameter("config")
        val filterExperimentConfiguration = File(config).toInstance<ControlledFilterExperimentConfiguration>()
        experiment = ControlledFilterExperimentBuilder(filterExperimentConfiguration).build()
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        experiment.run(request, response, chain)
    }

}
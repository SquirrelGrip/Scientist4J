package com.github.squirrelgrip.api.controller

import com.github.squirrelgrip.api.exception.ExperimentAlreadyExistsException
import com.github.squirrelgrip.api.exception.ExperimentNotFoundException
import com.github.squirrelgrip.scientist4k.http.server.HttpExperimentConfiguration
import com.github.squirrelgrip.scientist4k.http.server.HttpExperimentServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
class ExperimentController {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ExperimentController::class.java)

        val experiments: MutableMap<String, Experiment> = mutableMapOf()

        private fun getExperiment(name: String) = experiments[name] ?: throw ExperimentNotFoundException()
    }

    @GetMapping("/api/v1/experiments", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experiments.keys.toList()
    }

    @GetMapping("/api/v1/experiment/{name}", produces = ["application/json"])
    fun getExperimentByName(@PathVariable("name") name: String): Experiment {
        return getExperiment(name)
    }

    @PostMapping("/api/v1/experiment")
    fun createExperiment(@RequestBody httpExperimentConfiguration: HttpExperimentConfiguration) {
        if (experiments[httpExperimentConfiguration.experiment.name] != null) {
            throw ExperimentAlreadyExistsException()
        }
        experiments[httpExperimentConfiguration.experiment.name] = Experiment(httpExperimentConfiguration)
    }

    @PostMapping("/api/v1/experiment/{name}/start")
    fun startExperiment(@PathVariable("name") name: String) {
        return getExperiment(name).start()
    }

    @PostMapping("/api/v1/experiment/{name}/stop")
    fun stopExperiment(@PathVariable("name") name: String) {
        return getExperiment(name).stop()
    }

}

class Experiment(
        val configuration: HttpExperimentConfiguration
) {
    private val server: HttpExperimentServer = HttpExperimentServer(configuration)

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    val state: String
        get() = server.server.state
}

enum class Status {

}

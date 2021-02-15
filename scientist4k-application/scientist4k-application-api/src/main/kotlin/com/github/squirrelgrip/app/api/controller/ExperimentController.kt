package com.github.squirrelgrip.app.api.controller

import com.github.squirrelgrip.app.api.model.Experiment
import com.github.squirrelgrip.app.common.exception.ExperimentAlreadyExistsException
import com.github.squirrelgrip.app.common.exception.ExperimentNotFoundException
import com.github.squirrelgrip.extension.yaml.toInstance
import com.github.squirrelgrip.scientist4k.http.server.HttpExperimentConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.io.File

@RestController
class ExperimentController {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ExperimentController::class.java)

        private val listFiles = File("data").also { if (!it.exists()) it.mkdirs() }.listFiles { _, name -> name.endsWith(".json") }

        val experiments: MutableMap<String, Experiment> =
            listFiles.map {
                val httpExperimentConfiguration = it.toInstance<HttpExperimentConfiguration>()
                httpExperimentConfiguration.experiment.name to Experiment(httpExperimentConfiguration)
            }.toMap().toMutableMap()

        private fun getExperiment(name: String) = experiments[name] ?: throw ExperimentNotFoundException()
    }

    @GetMapping("/api/v1/experiments", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experiments.keys.toList()
    }

    @PostMapping("/api/v1/experiment")
    fun createExperiment(@RequestBody httpExperimentConfiguration: HttpExperimentConfiguration) {
        if (experiments[httpExperimentConfiguration.experiment.name] != null) {
            throw ExperimentAlreadyExistsException()
        }
        experiments[httpExperimentConfiguration.experiment.name] = Experiment(httpExperimentConfiguration)
    }

    @GetMapping("/api/v1/experiment/{name}", produces = ["application/json"])
    fun getExperimentByName(@PathVariable("name") name: String): Experiment {
        return getExperiment(name)
    }

    @GetMapping("/api/v1/experiment/{name}/status", produces = ["application/json"])
    fun getExperimentStatusByName(@PathVariable("name") name: String): String {
        return getExperiment(name).state
    }

    @PostMapping("/api/v1/experiment/{name}/start")
    fun startExperiment(@PathVariable("name") name: String) {
        return getExperiment(name).start()
    }

    @PostMapping("/api/v1/experiment/{name}/restart")
    fun restartExperiment(@PathVariable("name") name: String) {
        getExperiment(name).stop()
        return getExperiment(name).start()
    }

    @PostMapping("/api/v1/experiment/{name}/stop")
    fun stopExperiment(@PathVariable("name") name: String) {
        return getExperiment(name).stop()
    }

}

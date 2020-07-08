package com.github.squirrelgrip.report.controller

import com.github.squirrelgrip.report.model.Experiment
import com.github.squirrelgrip.report.repository.ExperimentRepository
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperimentController(
        val experimentRepository: ExperimentRepository
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ExperimentController::class.java)
    }

    @GetMapping("/api/v1/experiments", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experimentRepository.findAllExperiments().map { it.name }
    }

    @GetMapping("/api/v1/experiment/{name}", produces = ["application/json"])
    fun getExperimentsByName(@PathVariable("name") name: String): Experiment {
            return experimentRepository.findExperimentByName(name)
    }

    @GetMapping("/api/v1/experiment/{name}/results/{id}", produces = ["application/json"])
    fun getExperimentResultsByName(
            @PathVariable("name") name: String,
            @PathVariable("id") id: String
    ): HttpExperimentResult {
            return experimentRepository.findExperimentByName(name).getResult(id)
    }

}
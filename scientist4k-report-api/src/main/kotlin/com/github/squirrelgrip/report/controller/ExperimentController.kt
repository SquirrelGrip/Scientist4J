package com.github.squirrelgrip.report.controller

import com.github.squirrelgrip.report.model.ExperimentReport
import com.github.squirrelgrip.report.repository.ExperimentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class ExperimentController(
        val experimentRepository: ExperimentRepository
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ExperimentController::class.java)
    }
    @GetMapping("/api/v1/experiments", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experimentRepository.findAllExperiments()
    }

    @GetMapping("/api/v1/experiment/{name}", produces = ["application/json"])
    fun getExperimentsByName(@PathVariable("name") name: String): ExperimentReport {
        try {
            return experimentRepository.findExperimentByName(name)
        } catch(e: Exception) {
            LOGGER.info(e.message, e)
            throw ResponseStatusException(HttpStatus.NOT_FOUND,  "Experiment not found", e)
        }
    }

}
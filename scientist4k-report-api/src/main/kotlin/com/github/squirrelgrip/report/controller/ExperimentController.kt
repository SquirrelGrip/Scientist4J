package com.github.squirrelgrip.report.controller

import com.github.squirrelgrip.report.model.ExperimentSummary
import com.github.squirrelgrip.report.model.ExperimentUrl
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
    fun getExperiments(): List<ExperimentSummary> {
        return experimentRepository.findAllExperiments().map { it.experimentSummary }
    }

    @GetMapping("/api/v1/experiment/{name}", produces = ["application/json"])
    fun getExperimentsByName(@PathVariable("name") name: String): ExperimentSummary {
            return experimentRepository.findExperimentByName(name).experimentSummary
    }

    @GetMapping("/api/v1/experiment/{name}/urls", produces = ["application/json"])
    fun getExperimentUrlsByName(@PathVariable("name") name: String): List<ExperimentUrl> {
            return experimentRepository.findExperimentByName(name).experimentUrls
    }

    @GetMapping("/api/v1/experiment/{name}/{id}", produces = ["application/json"])
    fun getExperimentUrlsByName(
            @PathVariable("name") name: String,
            @PathVariable("id") id: String
    ): HttpExperimentResult {
            return experimentRepository.findExperimentByName(name).findById(id)
    }

}
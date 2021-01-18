package com.github.squirrelgrip.report.controller

import com.github.squirrelgrip.report.model.ExperimentResult
import com.github.squirrelgrip.report.repository.ExperimentReportRepository
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperimentReportController(
        val experimentReportRepository: ExperimentReportRepository
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ExperimentReportController::class.java)
    }

    @GetMapping("/api/v1/experiments/results", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experimentReportRepository.findAllExperiments().map { it.name }
    }

    @GetMapping("/api/v1/experiment/{name}/results", produces = ["application/json"])
    fun getExperimentsByName(@PathVariable("name") name: String): ExperimentResult {
            return experimentReportRepository.findExperimentByName(name)
    }

    @GetMapping("/api/v1/experiment/{name}/results/{id}", produces = ["application/json"])
    fun getExperimentResultsByName(
            @PathVariable("name") name: String,
            @PathVariable("id") id: String
    ): HttpExperimentResult {
            return experimentReportRepository.findExperimentByName(name).getResult(id)
    }

}
package com.github.squirrelgrip.api.controller

import com.github.squirrelgrip.api.model.ExperimentResults
import com.github.squirrelgrip.api.service.ExperimentReportService
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperimentReportController(
        val experimentReportService: ExperimentReportService
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ExperimentReportController::class.java)
    }

    @GetMapping("/api/v1/experiments/results", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return experimentReportService.findAllExperiments().map { it.name }
    }

    @GetMapping("/api/v1/experiment/results/{name}", produces = ["application/json"])
    fun getExperimentsByName(@PathVariable("name") name: String): ExperimentResults {
            return experimentReportService.getExperimentResultsByName(name)
    }

    @GetMapping("/api/v1/experiment/results/{name}/{id}", produces = ["application/json"])
    fun getExperimentResultsByName(
            @PathVariable("name") name: String,
            @PathVariable("id") id: String
    ): HttpExperimentResult {
        return experimentReportService.getExperimentResult(name, id)
    }

    @GetMapping("/api/v1/experiment/results/{name}/urls", produces = ["application/json"])
    fun getExperimentResultsByName(
            @PathVariable("name") name: String,
    ): List<String> {
        return experimentReportService.getExperimentResultUrls(name)
    }



}
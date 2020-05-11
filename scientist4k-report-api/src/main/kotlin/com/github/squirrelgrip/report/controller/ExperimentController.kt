package com.github.squirrelgrip.report.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperimentController {
    @GetMapping("/api/v1/experiments", produces = ["application/json"])
    fun getExperiments(): List<String> {
        return listOf("test")
    }
}
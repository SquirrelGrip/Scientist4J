package com.github.squirrelgrip.app.api.service

import com.github.squirrelgrip.app.api.model.ExperimentResults
import com.github.squirrelgrip.app.api.repository.ExperimentReportRepository
import com.github.squirrelgrip.scientist4k.http.core.model.HttpExperimentResult
import org.springframework.stereotype.Service

@Service
class ExperimentReportService(
    val experimentReportRepository: ExperimentReportRepository
) {
    fun findAllExperiments(): List<ExperimentResults> {
        return experimentReportRepository.findAllExperiments()
    }

    fun getExperimentResultsByName(name: String): ExperimentResults =
        experimentReportRepository.findExperimentByName(name)

    fun getExperimentResult(name: String, id: String): HttpExperimentResult =
        getExperimentResultsByName(name).getResult(id)

    fun getExperimentResultUrls(name: String) =
        getExperimentResultsByName(name).results
            .map {
                getExperimentResult(name, it)
            }.groupingBy {
                it.request.url
            }.eachCount()

}
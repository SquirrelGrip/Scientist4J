package com.github.squirrelgrip.api.service

import com.github.squirrelgrip.api.model.ExperimentResults
import com.github.squirrelgrip.api.repository.ExperimentReportRepository
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
            getExperimentResultsByName(name)

    fun getExperimentResult(name: String, id: String): HttpExperimentResult =
            getExperimentResultsByName(name).getResult(id)

    fun getExperimentResultUrls(name: String): List<String> {
        return getExperimentResultsByName(name).results
                .map {
                    getExperimentResult(name, it)
                }.map {
                    it.request.url
                }
    }

}
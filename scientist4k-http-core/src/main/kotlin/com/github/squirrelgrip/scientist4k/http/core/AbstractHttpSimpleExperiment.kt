package com.github.squirrelgrip.scientist4k.http.core

import com.github.squirrelgrip.scientist4k.core.comparator.NoopComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.simple.SimpleExperiment
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

open class AbstractHttpSimpleExperiment(
    experimentConfiguration: ExperimentConfiguration,
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    val mappingConfiguration: List<MappingConfiguration> = emptyList()
) : SimpleExperiment<ExperimentResponse>(
    experimentConfiguration,
    NoopComparator(),
    eventBus
) {
    fun getRunOptions(inboundRequest: ServletRequest): EnumSet<ExperimentOption> =
        if (inboundRequest is HttpServletRequest) {
            mappingConfiguration.firstOrNull {
                it.matches(inboundRequest.pathInfo)
            }?.options ?: ExperimentOption.DEFAULT
        } else {
            ExperimentOption.DEFAULT
        }

    override fun publish(result: Any, sample: Sample) {
        if (isPublishable(sample)) {
            if (result is SimpleExperimentResult<*> && result.control.value is ExperimentResponse) {
                @Suppress("UNCHECKED_CAST") val experimentResult =
                    (result as SimpleExperimentResult<ExperimentResponse>).toHttpExperimentResult()
                eventBus.post(experimentResult)
            } else {
                super.publish(result, sample)
            }
        }
    }

}
package com.github.squirrelgrip.scientist4k.http.controlled

import com.github.squirrelgrip.scientist4k.controlled.ControlledExperiment
import com.github.squirrelgrip.scientist4k.controlled.model.ControlledExperimentResult
import com.github.squirrelgrip.scientist4k.core.comparator.NoopComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.http.core.configuration.MappingConfiguration
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.google.common.eventbus.EventBus
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

open class AbstractControlledHttpExperiment(
    experimentConfiguration: ExperimentConfiguration,
    eventBus: EventBus = DEFAULT_EVENT_BUS,
    val mappingConfiguration: List<MappingConfiguration> = emptyList()
) : ControlledExperiment<ExperimentResponse>(
    experimentConfiguration,
    NoopComparator(),
    eventBus
) {
    override fun publish(result: Any, sample: Sample) {
        if (isPublishable(sample)) {
            if (result is ControlledExperimentResult<*>) {
                if (result.control.value is ExperimentResponse) {
                    @Suppress("UNCHECKED_CAST") val experimentResult =
                        (result as ControlledExperimentResult<ExperimentResponse>).toHttpExperimentResult()
                    eventBus.post(experimentResult)
                }
            } else {
                super.publish(result, sample)
            }
        }
    }

    fun getRunOptions(inboundRequest: ServletRequest): EnumSet<ExperimentOption> =
        if (inboundRequest is HttpServletRequest) {
            mappingConfiguration.firstOrNull {
                it.matches(inboundRequest.pathInfo)
            }?.options ?: ExperimentOption.DEFAULT
        } else {
            ExperimentOption.DEFAULT
        }

}
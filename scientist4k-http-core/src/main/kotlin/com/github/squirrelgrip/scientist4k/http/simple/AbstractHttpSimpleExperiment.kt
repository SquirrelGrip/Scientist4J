package com.github.squirrelgrip.scientist4k.http.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.NoopComparator
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.simple.SimpleExperiment
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import java.util.*

open class AbstractHttpSimpleExperiment(
    name: String,
    metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
    sampleFactory: SampleFactory = SampleFactory(),
    eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS,
    experimentFlags: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
) : SimpleExperiment<ExperimentResponse>(
    name,
    metrics,
    NoopComparator(),
    sampleFactory,
    eventBus,
    experimentFlags
) {
    override fun publish(result: Any) {
        if (result is SimpleExperimentResult<*> && result.control.value is ExperimentResponse) {
            eventBus.post((result as SimpleExperimentResult<ExperimentResponse>).toHttpExperimentResult())
        } else {
            super.publish(result)
        }
    }
}
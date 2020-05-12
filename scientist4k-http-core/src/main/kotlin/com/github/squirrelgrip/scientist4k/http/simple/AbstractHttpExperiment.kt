package com.github.squirrelgrip.scientist4k.http.simple

import com.github.squirrelgrip.scientist4k.core.AbstractExperiment
import com.github.squirrelgrip.scientist4k.core.comparator.NoopComparator
import com.github.squirrelgrip.scientist4k.core.model.sample.SampleFactory
import com.github.squirrelgrip.scientist4k.http.core.extension.toHttpExperimentResult
import com.github.squirrelgrip.scientist4k.http.core.model.ExperimentResponse
import com.github.squirrelgrip.scientist4k.metrics.MetricsProvider
import com.github.squirrelgrip.scientist4k.simple.Experiment
import com.github.squirrelgrip.scientist4k.simple.model.ExperimentResult
import com.google.common.eventbus.EventBus

open class AbstractHttpExperiment(
        name: String,
        metrics: MetricsProvider<*> = MetricsProvider.build("DROPWIZARD"),
        sampleFactory: SampleFactory = SampleFactory(),
        eventBus: EventBus = AbstractExperiment.DEFAULT_EVENT_BUS,
        enabled: Boolean = true,
        async: Boolean = true
) : Experiment<ExperimentResponse>(
        name,
        false,
        metrics,
        NoopComparator(),
        sampleFactory,
        eventBus,
        enabled,
        async

) {
    override fun publish(result: Any) {
        if (result is ExperimentResult<*> && result.control.value is ExperimentResponse) {
            eventBus.post((result as ExperimentResult<ExperimentResponse>).toHttpExperimentResult())
        } else {
            super.publish(result)
        }
    }
}
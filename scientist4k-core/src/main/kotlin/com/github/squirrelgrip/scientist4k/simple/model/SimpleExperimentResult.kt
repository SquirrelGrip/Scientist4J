package com.github.squirrelgrip.scientist4k.simple.model

import com.github.squirrelgrip.scientist4k.core.model.ExperimentObservation
import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.github.squirrelgrip.scientist4k.core.model.sample.Sample
import com.github.squirrelgrip.scientist4k.simple.SimpleExperiment

class SimpleExperimentResult<T>(
        private val experiment: SimpleExperiment<T>,
        control: ExperimentObservation<T>,
        candidate: ExperimentObservation<T>?,
        sample: Sample
): ExperimentResult<T>(
        experiment,
        control,
        candidate,
        sample
)
package com.github.squirrelgrip.scientist4k.controlled.model

interface ControlledPublisher<T> {
    fun publish(experimentResult: ControlledExperimentResult<T>)
}
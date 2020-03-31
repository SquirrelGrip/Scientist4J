package com.github.squirrelgrip.scientist4k.model

interface ExperimentComparator<T> : (T?, T?) -> Boolean

class DefaultExperimentComparator<T> : ExperimentComparator<T> {
    override fun invoke(control: T?, candidate: T?): Boolean = control == candidate
}
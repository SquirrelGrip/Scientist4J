package com.github.squirrelgrip.scientist4k.model

interface ControlledPublisher<T> {
    fun publish(result: ControlledResult<T>)
}
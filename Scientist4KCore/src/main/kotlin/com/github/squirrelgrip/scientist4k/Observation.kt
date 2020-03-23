package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.Timer

class Observation<T>(
        val name: String,
        private val timer: Timer
) {
    private var _exception: Exception? = null
    private var _value: T? = null

    val exception: Exception? by lazy {
        _exception
    }
    val value: T? by lazy {
        _value
    }

    fun setException(exception: Exception) {
        _exception = exception
    }

    fun setValue(value: T?) {
        _value = value
    }

    val duration: Long
        get() = timer.duration

    fun time(runnable: () -> Unit) {
        timer.record(runnable)
    }
}
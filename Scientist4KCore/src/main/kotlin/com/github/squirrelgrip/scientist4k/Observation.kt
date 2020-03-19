package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.metrics.Timer

class Observation<T>(
        val name: String,
        private val timer: Timer
) {
    private var _exception: Exception? = null

    val exception: Exception? by lazy {
        _exception
    }
    var value: T? = null

    fun setException(e: Exception) {
        _exception = e
    }

    val duration: Long
        get() = timer.duration

    fun time(runnable: () -> Unit) {
        timer.record(runnable)
    }
}
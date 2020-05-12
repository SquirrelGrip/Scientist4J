package com.github.squirrelgrip.scientist4k.core.model

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.squirrelgrip.scientist4k.metrics.Timer
import com.github.squirrelgrip.scientist4k.metrics.noop.NoopTimer
import com.github.squirrelgrip.util.initOnce

@JsonPropertyOrder (
        "name",
        "value",
        "exception",
        "duration",
        "status"
)
class Observation<T>(
        val name: String,
        private val timer: Timer
) {
    constructor(name: String): this(name, SCRAP_TIMER)

    private var _exception: Exception? = null
    private var _value: T? = null
    var status: ObservationStatus by initOnce()

    companion object {
        val SCRAP_TIMER: Timer = NoopTimer()
    }

    val exception: Exception? by lazy {
        _exception
    }
    val value: T? by lazy {
        _value
    }

    fun setException(exception: Exception) {
        _exception = exception
        status = ObservationStatus.COMPLETED
    }

    fun setValue(value: T?) {
        _value = value
        status = ObservationStatus.COMPLETED
    }

    val duration: Long
        get() = timer.duration

    fun time(function: () -> T?) {
        timer.record {
            try {
                setValue(function.invoke())
            } catch (e: Exception) {
                setException(e)
            }
        }
    }

    override fun toString(): String {
        return if (exception != null) {
            exception!!.message ?: exception!!.javaClass.toString()
        } else {
            value.toString()
        }
    }

}


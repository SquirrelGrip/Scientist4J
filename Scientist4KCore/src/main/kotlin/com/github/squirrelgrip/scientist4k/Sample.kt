package com.github.squirrelgrip.scientist4k

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class Sample {
    val startTime: Instant = Instant.now()
    val sampleId: String = "${sampleIdFactory.incrementAndGet()}"
    private val _notes: MutableList<String> = mutableListOf()
    val notes: List<String>
        get() = _notes.toList()

    fun add(note: String) {
        _notes.add(note)
    }

    companion object {
        private val sampleIdFactory = AtomicLong()
    }
}
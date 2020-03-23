package com.github.squirrelgrip.scientist4k

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class Note {
    val startTime: Instant = Instant.now()
    val sampleId: Long = sampleIdFactory.incrementAndGet()

    companion object {
        val sampleIdFactory = AtomicLong()
    }
}
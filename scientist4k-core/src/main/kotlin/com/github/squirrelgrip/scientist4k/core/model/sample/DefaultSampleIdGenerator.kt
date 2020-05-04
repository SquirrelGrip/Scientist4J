package com.github.squirrelgrip.scientist4k.core.model.sample

import java.util.concurrent.atomic.AtomicLong

class DefaultSampleIdGenerator : SampleIdGenerator {
    private val atomicLong = AtomicLong()

    override fun next(): String {
        return "${atomicLong.incrementAndGet()}"
    }

}

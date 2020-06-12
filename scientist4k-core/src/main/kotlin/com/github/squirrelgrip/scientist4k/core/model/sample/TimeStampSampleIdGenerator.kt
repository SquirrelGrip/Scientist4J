package com.github.squirrelgrip.scientist4k.core.model.sample

class TimeStampSampleIdGenerator : SampleIdGenerator {
    override fun next(): String {
        return "${System.currentTimeMillis()}"
    }

}

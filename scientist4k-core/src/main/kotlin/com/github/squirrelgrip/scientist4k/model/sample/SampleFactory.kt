package com.github.squirrelgrip.scientist4k.model.sample

import com.github.squirrelgrip.scientist4k.model.Sample

class SampleFactory(
        val prefix: String = "",
        val sampleIdGenerator: SampleIdGenerator = DefaultSampleIdGenerator()
) {
    fun create(): Sample {
        return Sample("${prefix}${sampleIdGenerator.next()}")
    }
}
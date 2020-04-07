package com.github.squirrelgrip.scientist4k.model.sample

class SampleFactory(
        val prefix: String = "",
        val sampleIdGenerator: SampleIdGenerator = DefaultSampleIdGenerator()
) {
    fun create(): Sample {
        return Sample("${prefix}${sampleIdGenerator.next()}")
    }
}
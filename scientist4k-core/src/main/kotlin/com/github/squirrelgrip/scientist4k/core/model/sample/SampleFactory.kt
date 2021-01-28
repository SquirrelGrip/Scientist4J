package com.github.squirrelgrip.scientist4k.core.model.sample

import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import java.util.*

class SampleFactory(
    val prefix: String = "",
    val sampleIdGenerator: SampleIdGenerator = DefaultSampleIdGenerator(),
    val sampleScoreGenerator: SampleScoreGenerator = DefaultSampleScoreGenerator()

) {
    fun create(runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT): Sample {
        return Sample("${prefix}${sampleIdGenerator.next()}", sampleScoreGenerator.getScore(), runOptions)
    }
}
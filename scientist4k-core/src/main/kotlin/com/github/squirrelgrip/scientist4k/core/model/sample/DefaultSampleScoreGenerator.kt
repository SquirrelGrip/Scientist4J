package com.github.squirrelgrip.scientist4k.core.model.sample

import kotlin.random.Random

class DefaultSampleScoreGenerator : SampleScoreGenerator {
    override fun getScore(): Int =
        Random.nextInt(0,100)

}

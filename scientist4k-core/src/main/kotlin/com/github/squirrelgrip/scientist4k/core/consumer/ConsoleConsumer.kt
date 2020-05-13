package com.github.squirrelgrip.scientist4k.core.consumer

import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.Subscribe

class ConsoleConsumer<T> {
    @Subscribe
    fun receiveResult(simpleExperimentResult: SimpleExperimentResult<T>) {
        println(simpleExperimentResult)
    }

}
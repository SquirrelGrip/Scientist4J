package com.github.squirrelgrip.scientist4k.core.consumer

import com.github.squirrelgrip.scientist4k.core.model.ExperimentResult
import com.google.common.eventbus.Subscribe

class ConsoleConsumer<T> {
    @Subscribe
    fun receiveResult(experimentResult: ExperimentResult<T>) {
        println(experimentResult)
    }

}
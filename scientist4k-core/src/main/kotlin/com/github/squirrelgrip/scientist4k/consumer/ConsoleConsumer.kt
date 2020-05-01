package com.github.squirrelgrip.scientist4k.consumer

import com.github.squirrelgrip.scientist4k.model.Result
import com.google.common.eventbus.Subscribe

class ConsoleConsumer<T> {
    @Subscribe
    fun receiveResult(result: Result<T>) {
        println(result)
    }

}
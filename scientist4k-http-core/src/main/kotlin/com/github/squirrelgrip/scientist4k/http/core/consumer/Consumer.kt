package com.github.squirrelgrip.scientist4k.http.core.consumer

interface Consumer<T> {
    fun receiveResult(experimentResult: T)
}
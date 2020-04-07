package com.github.squirrelgrip.scientist4k.model

interface Publisher<T> {
    fun publish(result: Result<T>)
}
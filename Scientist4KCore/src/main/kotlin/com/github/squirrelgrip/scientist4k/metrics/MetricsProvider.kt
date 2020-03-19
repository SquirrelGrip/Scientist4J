package com.github.squirrelgrip.scientist4k.metrics

interface MetricsProvider<T> {
    fun timer(vararg nameComponents: String): Timer
    fun counter(vararg nameComponents: String): Counter

    val registry: T
}
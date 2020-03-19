package com.github.squirrelgrip.scientist4k.metrics

interface Timer {
    fun record(runnable: () -> Unit)
    val duration: Long
}
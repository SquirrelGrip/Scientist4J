package com.github.rawls238.scientist4k.metrics

interface Timer {
    fun record(runnable: () -> Unit)
    var duration: Long
}
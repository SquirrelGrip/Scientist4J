package com.github.squirrelgrip.scientist4k.model

import java.time.Instant

class Sample(val sampleId: String) {
    val startTime: Instant = Instant.now()
    private val _notes: MutableList<String> = mutableListOf()
    val notes: List<String>
        get() = _notes.toList()

    fun add(note: String) {
        _notes.add(note)
    }

    companion object
}
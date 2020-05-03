package com.github.squirrelgrip.scientist4k.core.model.sample

import java.time.Instant

class Sample(val id: String) {
    val startTime: Instant = Instant.now()
    private val _notes: MutableMap<String, String> = mutableMapOf()
    val notes: Map<String, String>
        get() = _notes.toMap()

    fun addNote(noteKey: String, note: String) {
        _notes[noteKey] = note
    }

}
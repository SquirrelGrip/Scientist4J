package com.github.squirrelgrip.scientist4k.core.model.sample

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

class Sample(
        @JsonProperty("id")
        val id: String
) {
    @JsonProperty("startTime")
    val startTime: Instant = Instant.now()

    private val _notes: MutableMap<String, Any> = mutableMapOf()

    val notes: Map<String, Any>
        @JsonProperty("notes")
        get() = _notes.toMap()

    fun addNote(noteKey: String, note: Any) {
        _notes[noteKey] = note
    }

}
package com.github.squirrelgrip.scientist4k.core.model.sample

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import java.time.Instant
import java.util.*

data class Sample(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("sampleScore")
    val sampleScore: Int,
    @JsonProperty("runOptions")
    val runOptions: EnumSet<ExperimentOption> = ExperimentOption.DEFAULT
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
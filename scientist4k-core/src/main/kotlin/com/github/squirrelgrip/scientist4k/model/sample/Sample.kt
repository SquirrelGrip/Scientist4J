package com.github.squirrelgrip.scientist4k.model.sample

import org.awaitility.Awaitility
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class Sample(val id: String) {
    val startTime: Instant = Instant.now()
    val published: AtomicBoolean = AtomicBoolean(false)
    val generated: AtomicBoolean = AtomicBoolean(false)
    private val _notes: MutableMap<String, String> = mutableMapOf()
    val notes: Map<String, String>
        get() = _notes.toMap()

    companion object {
        val conditionFactory = Awaitility.await()
                .pollExecutorService(Executors.newFixedThreadPool(4))
    }

    fun addNote(noteKey: String, note: String) {
        _notes[noteKey] = note
    }

    fun awaitGenerated() {
        conditionFactory.untilTrue(generated)
    }

    fun awaitGenerated(timeout: Long, unit: TimeUnit) {
        conditionFactory.atMost(timeout, unit).untilTrue(generated)
    }

    fun awaitPublished() {
        conditionFactory.untilTrue(published)
    }

    fun awaitPublished(timeout: Long, unit: TimeUnit) {
        conditionFactory.atMost(timeout, unit).untilTrue(published)
    }

}
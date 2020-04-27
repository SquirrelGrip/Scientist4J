package com.github.squirrelgrip.scientist4k.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.properties.ReadWriteProperty

inline fun <reified T> initOnce(): ReadWriteProperty<Any, T> = InitOnceProperty()

class InitOncePropertyTest {

    var property: String by initOnce()

    @Test
    fun readValueFailure() {
        assertThrows(IllegalStateException::class.java) {  val data = property }
    }

    @Test
    fun writeValueTwice() {
        property = "Test1"
        assertThrows(IllegalStateException::class.java) {  property = "Test2" }
    }

    @Test
    fun readWriteCorrect() {
        property = "Test"
        val data1 = property
        val data2 = property
    }

}
package com.github.squirrelgrip.scientist4k.configuration

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class MappingConfigurationTest {

    @Test
    fun `replace a with b`() {
        val testSubject = MappingConfiguration("(a)", "b")
        assertThat(testSubject.matches("b")).isFalse()
        assertThat(testSubject.matches("a")).isTrue()
        assertThat(testSubject.replace("a")).isEqualTo("b")
    }

    @Test
    fun `replace all with b`() {
        val testSubject = MappingConfiguration("a", "b")
        assertThat(testSubject.matches("b")).isFalse()
        assertThat(testSubject.matches("a")).isTrue()
        assertThat(testSubject.replace("a")).isEqualTo("b")
    }

    @Test
    fun `swap a and b`() {
        val testSubject = MappingConfiguration("(a)(b)", "$2$1")
        assertThat(testSubject.matches("ba")).isFalse()
        assertThat(testSubject.matches("ab")).isTrue()
        assertThat(testSubject.replace("ab")).isEqualTo("ba")
    }

    @Test
    fun `append b when a`() {
        val testSubject = MappingConfiguration("(a)", "$1b")
        assertThat(testSubject.matches("b")).isFalse()
        assertThat(testSubject.matches("a")).isTrue()
        assertThat(testSubject.replace("a")).isEqualTo("ab")
    }
}
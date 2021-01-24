package com.github.squirrelgrip.scientist4k.http.core.configuration

import com.github.squirrelgrip.extension.json.toInstance
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import java.util.*

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

    @Test
    fun `with options`() {
        val testSubject = MappingConfiguration("a", "a", EnumSet.of(ExperimentOption.RETURN_CANDIDATE))
        println(testSubject.toJson())
        val testSubject2 = testSubject.toJson().toInstance<MappingConfiguration>()
        println(testSubject2.toJson())
    }
}

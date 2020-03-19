package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.exceptions.MismatchException
import com.github.squirrelgrip.scientist4k.metrics.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.MicrometerMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import io.dropwizard.metrics5.MetricName
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.util.*
import java.util.function.BiFunction

class ExperimentTest {
    private fun exceptionThrowingFunction(): Int {
        throw ExpectingAnException("throw an exception")
    }

    private fun safeFunction(): Int {
        return 3
    }

    private fun sleepFunction(): Int {
        Thread.sleep(1001)
        return 3
    }

    private fun safeFunctionWithDifferentResult(): Int {
        return 4
    }

    @Test
    fun itThrowsAnExceptionWhenControlFails() {
        assertThrows(ExpectingAnException::class.java) {
            Experiment<Int>("test", NoopMetricsProvider()).run({ exceptionThrowingFunction() }) { exceptionThrowingFunction() }
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        val `val` = experiment.run({ safeFunction() }) { exceptionThrowingFunction() }
        Assertions.assertThat(`val`).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        assertThrows(MismatchException::class.java) {
            Experiment<Int>("test", true, NoopMetricsProvider()).run({ safeFunction() }) { safeFunctionWithDifferentResult() }
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val `val` = Experiment<Int>("test", true, NoopMetricsProvider())
                .run({ safeFunction() }) { safeFunction() }
        Assertions.assertThat(`val`).isEqualTo(3)
    }

    @Test
    fun itHandlesNullValues() {
        val `val` = Experiment<Int?>("test", true, NoopMetricsProvider()).run({ null }) { null }
        Assertions.assertThat(`val`).isNull()
    }

    @Test
    fun nonAsyncRunsLongTime() {
        val exp = Experiment<Int>("test", true, NoopMetricsProvider())
        val date1 = Date()
        val value = exp.runSync({ sleepFunction() }) { sleepFunction() }
        val date2 = Date()
        val difference = date2.time - date1.time
        Assertions.assertThat(difference).isGreaterThanOrEqualTo(2000)
        Assertions.assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val exp: Experiment<Int> = TestPublishExperiment("test", NoopMetricsProvider())
        exp.run({ safeFunction() }) { safeFunction() }
    }

    @Test
    fun candidateExceptionsAreCounted_dropwizard() {
        val provider = DropwizardMetricsProvider()
        val exp = Experiment<Int>("test", provider)
        exp.run({ 1 }) { exceptionThrowingFunction() }
        val result = provider.registry.counters[MetricName.build("scientist", "test", "candidate", "exception")]
        Assertions.assertThat(result!!.count).isEqualTo(1)
    }

    @Test
    fun candidateExceptionsAreCounted_micrometer() {
        val provider = MicrometerMetricsProvider()
        val exp = Experiment<Int>("test", provider)
        exp.run({ 1 }) { exceptionThrowingFunction() }
        val result = provider.registry["scientist.test.candidate.exception"].counter()
        Assertions.assertThat(result.count()).isEqualTo(1.0)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun shouldUseCustomComparator() {
        val comparator: BiFunction<Int?, Int?, Boolean> = Mockito.mock<BiFunction<*, *, *>>(BiFunction::class.java) as BiFunction<Int?, Int?, Boolean>
        BDDMockito.given(comparator.apply(1, 2)).willReturn(false)
        val e = ExperimentBuilder<Int>()
                .withName("test")
                .withComparator(comparator)
                .withMetricsProvider(NoopMetricsProvider())
                .build()
        e.run({ 1 }) { 2 }
        Mockito.verify(comparator).apply(1, 2)
    }
}

internal class ExpectingAnException(message: String) : RuntimeException(message)
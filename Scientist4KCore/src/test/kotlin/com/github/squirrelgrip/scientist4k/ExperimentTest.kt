package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.exceptions.MismatchException
import com.github.squirrelgrip.scientist4k.metrics.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.MicrometerMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import io.dropwizard.metrics5.MetricName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.verify
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
            Experiment<Int>("test", NoopMetricsProvider()).run({ exceptionThrowingFunction() }, { exceptionThrowingFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        val value = experiment.run({ safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        assertThrows(MismatchException::class.java) {
            experiment.run({ safeFunction() }, { safeFunctionWithDifferentResult() })
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        val value = experiment.run({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itHandlesNullValues() {
        val experiment = Experiment<Int?>("test", true, NoopMetricsProvider())
        val value = experiment.run({ null }, { null })
        assertThat(value).isNull()
    }

    @Test
    fun nonAsyncRunsLongTime() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        val date1 = Date()
        val value = experiment.runSync({ sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        val difference = date2.time - date1.time
        assertThat(difference).isGreaterThanOrEqualTo(2000)
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val experiment: Experiment<Int> = TestPublishExperiment("test", NoopMetricsProvider())
        experiment.run({ safeFunction() }, { safeFunction() })
    }

    @Test
    fun candidateExceptionsAreCounted_dropwizard() {
        val provider = DropwizardMetricsProvider()
        val experiment = Experiment<Int>("test", provider)
        experiment.run({ 1 }, { exceptionThrowingFunction() })
        val result = provider.registry.counters[MetricName.build("scientist", "test", "candidate", "exception")]
        assertThat(result!!.count).isEqualTo(1)
    }

    @Test
    fun candidateExceptionsAreCounted_micrometer() {
        val provider = MicrometerMetricsProvider()
        val experiment = Experiment<Int>("test", provider)
        experiment.run({ 1 }, { exceptionThrowingFunction() })
        val result = provider.registry["scientist.test.candidate.exception"].counter()
        assertThat(result.count()).isEqualTo(1.0)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun shouldUseCustomComparator() {
        val comparator: BiFunction<Int?, Int?, Boolean> = Mockito.mock<BiFunction<*, *, *>>(BiFunction::class.java) as BiFunction<Int?, Int?, Boolean>
        given(comparator.apply(1, 2)).willReturn(false)
        val experiment = ExperimentBuilder<Int>()
                .withName("test")
                .withComparator(comparator)
                .withMetricsProvider(NoopMetricsProvider())
                .build()
        experiment.run({ 1 }, { 2 })
        verify(comparator).apply(1, 2)
    }
}
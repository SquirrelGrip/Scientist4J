package com.github.squirrelgrip.scientist4k.core

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ExperimentComparator
import com.github.squirrelgrip.scientist4k.metrics.dropwizard.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.micrometer.MicrometerMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.noop.NoopMetricsProvider
import com.google.common.eventbus.EventBus
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.dropwizard.metrics5.MetricName
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.isA
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import java.util.*

class ExperimentTest {
    private fun exceptionThrowingFunction(): Int {
        throw Exception("throw an exception")
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
        assertThrows(Exception::class.java) {
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
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
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
        val comparator = mock<ExperimentComparator<Int?>>()
        given(comparator.invoke(1, 2)).willReturn(ComparisonResult("An error"))
        val experiment = ExperimentBuilder<Int>()
                .withName("test")
                .withComparator(comparator)
                .withMetricsProvider(NoopMetricsProvider())
                .build()
        experiment.run({ 1 }, { 2 })
        verify(comparator).invoke(eq(1), eq(2))
    }

    @Test
    fun `build() using ExperimentConfiguration`() {
        val experimentConfiguration = ExperimentConfiguration(
                "name",
                true,
                "NOOP",
                emptyMap(),
                "prefix"
        )
        val experiment = ExperimentBuilder<Int>(experimentConfiguration).build()

        assertThat(experiment.name).isEqualTo("name")
        assertThat(experiment.raiseOnMismatch).isEqualTo(true)
        assertThat(experiment.metrics.javaClass).isEqualTo(NoopMetricsProvider::class.java)
        assertThat(experiment.sampleFactory.prefix).isEqualTo("prefix")
    }

    @Test
    fun `eventBus is called`() {
        val eventBus = mock<EventBus>()
        val experiment = ExperimentBuilder<Int>()
                .withRaiseOnMismatch(true)
                .withEventBus(eventBus)
                .build()
        experiment.run({safeFunction()}, {safeFunction()})
        verify(eventBus).post(isA(Result::class.java))
    }
}
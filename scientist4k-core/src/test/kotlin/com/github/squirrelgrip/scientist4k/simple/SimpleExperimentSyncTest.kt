package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.comparator.ExperimentComparator
import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.ComparisonResult
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption.*
import com.github.squirrelgrip.scientist4k.metrics.dropwizard.DropwizardMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.micrometer.MicrometerMetricsProvider
import com.github.squirrelgrip.scientist4k.metrics.noop.NoopMetricsProvider
import com.github.squirrelgrip.scientist4k.simple.model.SimpleExperimentResult
import com.google.common.eventbus.EventBus
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.dropwizard.metrics5.MetricName
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verifyNoInteractions
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class SimpleExperimentSyncTest {
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

    @Captor
    lateinit var argumentCaptor: ArgumentCaptor<SimpleExperimentResult<*>>

    @Mock
    lateinit var eventBus : EventBus

    @Test
    fun itThrowsAnExceptionWhenControlFails() {
        assertThrows(Exception::class.java) {
            SimpleExperiment<Int>("test", NoopMetricsProvider()).runSync(
                { exceptionThrowingFunction() },
                { exceptionThrowingFunction() })
        }
    }

    @Test
    fun itThrowsAnExceptionWhenControlFailsAndCandidateDoesNotFail() {
        assertThrows(Exception::class.java) {
            SimpleExperiment<Int>("test", NoopMetricsProvider()).runSync(
                { exceptionThrowingFunction() },
                { safeFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runSync({ safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateReturnsDifferentValue() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runSync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itCandidateReturnsDifferentValueAndCandidateIsReturned() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RETURN_CANDIDATE))
        val value = experiment.runSync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        assertThat(value).isEqualTo(4)
    }

    @Test
    fun candidateIsReturnedWhenDisabled() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider(), eventBus = eventBus, experimentOptions = EnumSet.of(RETURN_CANDIDATE, DISABLED))
        val value = experiment.runSync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        assertThat(value).isEqualTo(4)
        verify(eventBus).post(argumentCaptor.capture())
        assertThat(argumentCaptor.value.candidate?.value).isEqualTo(4)
        assertThat(argumentCaptor.value.control?.value).isNull()
    }

    @Test
    fun itThrowsAnExceptionWhenCandidateFailsAndIsReturned() {
        assertThrows(Exception::class.java) {
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RETURN_CANDIDATE))
                .runSync({ safeFunction() }, { exceptionThrowingFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateThrowsException() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runSync({ safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RAISE_ON_MISMATCH))
        assertThrows(MismatchException::class.java) {
            experiment.runSync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RAISE_ON_MISMATCH))
        val value = experiment.runSync({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itHandlesNullValues() {
        val experiment =
            SimpleExperiment<Int?>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RAISE_ON_MISMATCH))
        val value = experiment.runSync({ null }, { null })
        assertThat(value).isNull()
    }

    @Test
    fun nonAsyncRunsLongTime() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentOptions = EnumSet.of(RAISE_ON_MISMATCH))
        val date1 = Date()
        val value = experiment.runSync({ sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        val difference = date2.time - date1.time
        assertThat(difference).isGreaterThanOrEqualTo(2000)
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        experiment.runSync({ safeFunction() }, { safeFunction() })
    }

    @Test
    fun candidateExceptionsAreCounted_dropwizard() {
        val provider = DropwizardMetricsProvider()
        val experiment = SimpleExperiment<Int>("test", provider)
        experiment.runSync({ 1 }, { exceptionThrowingFunction() })
        val result = provider.registry.counters[MetricName.build("scientist", "test", "candidate", "exception")]
        assertThat(result!!.count).isEqualTo(1)
    }

    @Test
    fun candidateExceptionsAreCounted_micrometer() {
        val provider = MicrometerMetricsProvider()
        val experiment = SimpleExperiment<Int>("test", provider)
        experiment.runSync({ 1 }, { exceptionThrowingFunction() })
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until {
            val result = provider.registry["scientist.test.candidate.exception"].counter()
            result.count().equals(1.0)
        }
        val result = provider.registry["scientist.test.candidate.exception"].counter()
        assertThat(result.count()).isEqualTo(1.0)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun shouldUseCustomComparator() {
        val comparator = mock<ExperimentComparator<Int?>>()
        given(comparator.invoke(1, 2)).willReturn(ComparisonResult("An error"))
        val experiment = SimpleExperimentBuilder<Int>()
            .withName("test")
            .withComparator(comparator)
            .withMetricsProvider(NoopMetricsProvider())
            .build()
        experiment.runSync({ 1 }, { 2 })
        verify(comparator).invoke(eq(1), eq(2))
    }

    @Test
    fun `build() using ExperimentConfiguration`() {
        val experimentConfiguration = ExperimentConfiguration(
            "name",
            "NOOP",
            emptyMap(),
            "prefix",
            EnumSet.of(RAISE_ON_MISMATCH)
        )
        val experiment = SimpleExperimentBuilder<Int>(experimentConfiguration).build()

        assertThat(experiment.name).isEqualTo("name")
        assertThat(experiment.metrics.javaClass).isEqualTo(NoopMetricsProvider::class.java)
        assertThat(experiment.sampleFactory.prefix).isEqualTo("prefix")
    }

    @Test
    fun `eventBus is called`() {
        val experiment = SimpleExperimentBuilder<Int>()
            .withEventBus(eventBus)
            .build()
        experiment.run({ safeFunction() }, { safeFunction() })
        verify(eventBus).post(argumentCaptor.capture())
        println(argumentCaptor.value)
    }

    @Test
    fun `eventBus is not called when WITHHOLD_PUBLICATION`() {
        val experiment = SimpleExperimentBuilder<Int>()
            .withExperimentOptions(WITHHOLD_PUBLICATION)
            .withEventBus(eventBus)
            .build()
        experiment.run({ safeFunction() }, { safeFunction() })
        verifyNoInteractions(eventBus)
    }

    @Test
    fun `eventBus is not called with sampleThreshold is zero`() {
        val experiment = SimpleExperimentBuilder<Int>()
            .withSampleThreshold(0)
            .withEventBus(eventBus)
            .build()
        experiment.run({ safeFunction() }, { safeFunction() })
        verifyNoInteractions(eventBus)
    }
}
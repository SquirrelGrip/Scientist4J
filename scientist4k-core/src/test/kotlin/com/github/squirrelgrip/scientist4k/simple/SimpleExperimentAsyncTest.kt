package com.github.squirrelgrip.scientist4k.simple

import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag.RAISE_ON_MISMATCH
import com.github.squirrelgrip.scientist4k.core.model.ExperimentFlag.RETURN_CANDIDATE
import com.github.squirrelgrip.scientist4k.metrics.noop.NoopMetricsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class SimpleExperimentAsyncTest {
    private fun exceptionThrowingFunction(): Int {
        throw RuntimeException("throw an exception")
    }

    private fun sleepFunction(): Int {
        Thread.sleep(1001)
        return 3
    }

    private fun shortSleepFunction(): Int {
        Thread.sleep(101)
        return 3
    }

    private fun safeFunction(): Int {
        return 3
    }

    private fun safeFunctionWithDifferentResult(): Int {
        return 4
    }

    @Test
    fun itThrowsAnExceptionWhenControlFails() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        assertThrows(RuntimeException::class.java) {
            experiment.runAsync({ exceptionThrowingFunction() }, { exceptionThrowingFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runAsync({ safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itCandidateReturnsDifferentValueAndCandidateIsReturned() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentFlags = EnumSet.of(RETURN_CANDIDATE))
        val value = experiment.runAsync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        assertThat(value).isEqualTo(4)
    }

    @Test
    fun itThrowsAnExceptionWhenCandidateFailsAndIsReturned() {
        assertThrows(Exception::class.java) {
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentFlags = EnumSet.of(RETURN_CANDIDATE))
                .runAsync({ safeFunction() }, { exceptionThrowingFunction() })
        }
    }


    @Test
    fun itThrowsOnMismatch() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentFlags = EnumSet.of(RAISE_ON_MISMATCH))
        assertThrows(MismatchException::class.java) {
            experiment.runAsync({ safeFunction() }, { safeFunctionWithDifferentResult() })
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentFlags = EnumSet.of(RAISE_ON_MISMATCH))
        val value = experiment.runAsync({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val experiment = SimpleExperiment<Int>("test", NoopMetricsProvider())
        val value = experiment.run({ safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun asyncRunsFaster() {
        val experiment =
            SimpleExperiment<Int>("test", NoopMetricsProvider(), experimentFlags = EnumSet.of(RAISE_ON_MISMATCH))
        val date1 = Date()
        val value = experiment.runAsync({ sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        val difference = date2.time - date1.time
        assertThat(difference).isLessThan(2000)
        assertThat(difference).isGreaterThanOrEqualTo(1000)
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun raiseOnMismatchRunsSlower() {
        val raisesOnMismatch =
            SimpleExperiment<Int>("raise", NoopMetricsProvider(), experimentFlags = EnumSet.of(RAISE_ON_MISMATCH))
        val doesNotRaiseOnMismatch = SimpleExperiment<Int>("does not raise", NoopMetricsProvider())
        val raisesExecutionTime = timeExperiment(raisesOnMismatch)
        val doesNotRaiseExecutionTime = timeExperiment(doesNotRaiseOnMismatch)
        assertThat(raisesExecutionTime).isGreaterThan(doesNotRaiseExecutionTime)
        assertThat(raisesExecutionTime).isGreaterThan(1000)
        assertThat(doesNotRaiseExecutionTime).isLessThan(200)
    }

    private fun timeExperiment(simpleExperiment: SimpleExperiment<Int>): Long {
        val date1 = Date()
        simpleExperiment.runAsync({ shortSleepFunction() }, { sleepFunction() })
        val date2 = Date()
        return date2.time - date1.time
    }
}
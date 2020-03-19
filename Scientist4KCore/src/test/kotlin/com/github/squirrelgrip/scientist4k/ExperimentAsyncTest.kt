package com.github.squirrelgrip.scientist4k

import com.github.squirrelgrip.scientist4k.exceptions.MismatchException
import com.github.squirrelgrip.scientist4k.metrics.NoopMetricsProvider
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class ExperimentAsyncTest {
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
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        assertThrows(RuntimeException::class.java) { experiment.runAsync({ exceptionThrowingFunction() }) { exceptionThrowingFunction() } }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment = Experiment<Int>("test", NoopMetricsProvider())
        val value = experiment.runAsync({ safeFunction() }) { exceptionThrowingFunction() }
        Assertions.assertThat(value).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        val experiment = Experiment<Int>("test", true, NoopMetricsProvider())
        assertThrows(MismatchException::class.java) {
            experiment.runAsync({ safeFunction() }) { safeFunctionWithDifferentResult() }
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val exp = Experiment<Int>("test", true, NoopMetricsProvider())
        val value = exp.runAsync({ safeFunction() }) { safeFunction() }
        Assertions.assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val value = TestPublishExperiment<Int>("test", NoopMetricsProvider()).run({ safeFunction() }) { safeFunction() }
        Assertions.assertThat(value).isEqualTo(3)
    }

    @Test
    fun asyncRunsFaster() {
        val exp = Experiment<Int>("test", true, NoopMetricsProvider())
        val date1 = Date()
        val value = exp.runAsync({ sleepFunction() }) { sleepFunction() }
        val date2 = Date()
        val difference = date2.time - date1.time
        Assertions.assertThat(difference).isLessThan(2000)
        Assertions.assertThat(difference).isGreaterThanOrEqualTo(1000)
        Assertions.assertThat(value).isEqualTo(3)
    }

    @Test
    fun raiseOnMismatchRunsSlower() {
        val raisesOnMismatch = Experiment<Int>("raise", true, NoopMetricsProvider())
        val doesNotRaiseOnMismatch = Experiment<Int>("does not raise", NoopMetricsProvider())
        val raisesExecutionTime = timeExperiment(raisesOnMismatch)
        val doesNotRaiseExecutionTime = timeExperiment(doesNotRaiseOnMismatch)
        Assertions.assertThat(raisesExecutionTime).isGreaterThan(doesNotRaiseExecutionTime)
        Assertions.assertThat(raisesExecutionTime).isGreaterThan(1000)
        Assertions.assertThat(doesNotRaiseExecutionTime).isLessThan(200)
    }

    private fun timeExperiment(exp: Experiment<Int>): Long {
        val date1 = Date()
        exp.runAsync({ shortSleepFunction() }) { sleepFunction() }
        val date2 = Date()
        return date2.time - date1.time
    }
}
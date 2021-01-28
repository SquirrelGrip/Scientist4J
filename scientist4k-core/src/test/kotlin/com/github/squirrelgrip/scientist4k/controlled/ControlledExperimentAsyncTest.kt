package com.github.squirrelgrip.scientist4k.controlled

import com.github.squirrelgrip.scientist4k.core.configuration.ExperimentConfiguration
import com.github.squirrelgrip.scientist4k.core.exception.MismatchException
import com.github.squirrelgrip.scientist4k.core.model.ExperimentOption.RAISE_ON_MISMATCH
import com.github.squirrelgrip.scientist4k.metrics.Metrics.NOOP
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class ControlledExperimentAsyncTest {
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
        val experiment =
            ControlledExperiment<Int>(ExperimentConfiguration("test", NOOP))
        assertThrows(RuntimeException::class.java) {
            experiment.runAsync({ exceptionThrowingFunction() }, { safeFunction() }, { safeFunction() })
        }
    }

    @Test
    fun itDoesntThrowAnExceptionWhenCandidateFails() {
        val experiment =
            ControlledExperiment<Int>(ExperimentConfiguration("test", NOOP))
        val value = experiment.runAsync({ safeFunction() }, { safeFunction() }, { exceptionThrowingFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itThrowsOnMismatch() {
        val experiment =
            ControlledExperiment<Int>(
                ExperimentConfiguration(
                    "test",
                    NOOP,
                    experimentOptions = EnumSet.of(RAISE_ON_MISMATCH)
                )
            )
        assertThrows(MismatchException::class.java) {
            experiment.runAsync({ safeFunction() }, { safeFunction() }, { safeFunctionWithDifferentResult() })
        }
    }

    @Test
    fun itDoesNotThrowOnMatch() {
        val experiment =
            ControlledExperiment<Int>(
                ExperimentConfiguration("test", NOOP, experimentOptions = EnumSet.of(RAISE_ON_MISMATCH))
            )
        val value = experiment.runAsync({ safeFunction() }, { safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun itWorksWithAnExtendedClass() {
        val experiment =
            ControlledExperiment<Int>(ExperimentConfiguration("test", NOOP))
        val value = experiment.run({ safeFunction() }, { safeFunction() }, { safeFunction() })
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun asyncRunsFaster() {
        val experiment =
            ControlledExperiment<Int>(
                ExperimentConfiguration(
                    "test",
                    NOOP,
                    experimentOptions = EnumSet.of(RAISE_ON_MISMATCH)
                )
            )
        val date1 = Date()
        val value = experiment.runAsync({ sleepFunction() }, { sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        val difference = date2.time - date1.time
        assertThat(difference).isLessThan(2500)
        assertThat(difference).isGreaterThanOrEqualTo(1000)
        assertThat(value).isEqualTo(3)
    }

    @Test
    fun raiseOnMismatchRunsSlower() {
        val raisesOnMismatch =
            ControlledExperiment<Int>(
                ExperimentConfiguration(
                    "raise",
                    NOOP,
                    experimentOptions = EnumSet.of(RAISE_ON_MISMATCH)
                )
            )
        val doesNotRaiseOnMismatch = ControlledExperiment<Int>(
            ExperimentConfiguration(
                "does not raise",
                NOOP
            )
        )
        val raisesExecutionTime = timeExperiment(raisesOnMismatch)
        val doesNotRaiseExecutionTime = timeExperiment(doesNotRaiseOnMismatch)
        assertThat(raisesExecutionTime).isGreaterThan(doesNotRaiseExecutionTime)
        assertThat(raisesExecutionTime).isGreaterThan(1000)
        assertThat(doesNotRaiseExecutionTime).isLessThan(200)
    }

    private fun timeExperiment(experiment: ControlledExperiment<Int>): Long {
        val date1 = Date()
        experiment.runAsync({ shortSleepFunction() }, { sleepFunction() }, { sleepFunction() })
        val date2 = Date()
        return date2.time - date1.time
    }
}
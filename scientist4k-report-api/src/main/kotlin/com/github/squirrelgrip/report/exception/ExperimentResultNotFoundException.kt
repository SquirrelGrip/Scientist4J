package com.github.squirrelgrip.report.exception

class ExperimentResultNotFoundException(
        val expectedId: String
): Exception("ExperimentResult with id $expectedId cannot be found.")
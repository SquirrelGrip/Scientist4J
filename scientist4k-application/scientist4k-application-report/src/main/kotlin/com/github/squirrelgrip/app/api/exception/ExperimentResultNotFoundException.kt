package com.github.squirrelgrip.app.api.exception

class ExperimentResultNotFoundException(
        val expectedId: String
): Exception("ExperimentResult with id $expectedId cannot be found.")
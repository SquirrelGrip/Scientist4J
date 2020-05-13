package com.github.squirrelgrip.report.controller

import com.github.squirrelgrip.report.exception.ExperimentNotFoundException
import com.github.squirrelgrip.report.exception.ExperimentResultNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [ExperimentNotFoundException::class])
    fun handleExperimentNotFoundException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Experiment not found"
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(value = [ExperimentResultNotFoundException::class])
    fun handleExperimentResultNotFoundException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Experiment Result not found"
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }
}
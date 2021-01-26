package com.github.squirrelgrip.app.common.controller

import com.github.squirrelgrip.app.common.exception.ExperimentAlreadyExistsException
import com.github.squirrelgrip.app.common.exception.ExperimentNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CommonResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [ExperimentNotFoundException::class])
    fun handleExperimentNotFoundException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Experiment not found"
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(value = [ExperimentAlreadyExistsException::class])
    fun handleExperimentAlreadyExistsException(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        val bodyOfResponse = "Experiment already exists"
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }
}
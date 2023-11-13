package com.gugas

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class ApplicationExceptionHandler {

    fun handleException(e: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
    }
}
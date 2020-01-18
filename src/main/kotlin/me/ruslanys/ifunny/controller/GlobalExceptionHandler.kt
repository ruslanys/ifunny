package me.ruslanys.ifunny.controller

import me.ruslanys.ifunny.controller.dto.ErrorDto
import me.ruslanys.ifunny.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ServerWebExchange

/**
 * FYI possible ways for Error Handling customization:
 * - Define a bean of type [org.springframework.boot.web.reactive.error.ErrorAttributes].
 * - Define a bean of type [org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler].
 */
@ControllerAdvice
@ResponseBody
class GlobalExceptionHandler {

    @ExceptionHandler
    fun handleIllegalArgumentException(exchange: ServerWebExchange, exception: IllegalArgumentException): ResponseEntity<Any> {
        return handleException(HttpStatus.BAD_REQUEST, exchange, exception)
    }

    @ExceptionHandler
    fun handleNotFoundException(exchange: ServerWebExchange, exception: NotFoundException): ResponseEntity<Any> {
        return handleException(HttpStatus.NOT_FOUND, exchange, exception)
    }

    private fun handleException(status: HttpStatus, exchange: ServerWebExchange, exception: Exception): ResponseEntity<Any> {
        return if (MediaType.APPLICATION_JSON.isCompatibleWith(exchange.request.headers.accept.firstOrNull())) {
            ResponseEntity(ErrorDto(exchange.request.path.toString(), status, exception.message), HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(exception.message, status)
        }
    }

}

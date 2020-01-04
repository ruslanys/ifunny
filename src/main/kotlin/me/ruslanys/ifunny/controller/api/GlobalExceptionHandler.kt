package me.ruslanys.ifunny.controller.api

import me.ruslanys.ifunny.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(request: HttpServletRequest, response: HttpServletResponse, ex: Throwable) {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.message)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(request: HttpServletRequest, response: HttpServletResponse, ex: Throwable) {
        response.sendError(HttpStatus.NOT_FOUND.value(), ex.message)
    }

}

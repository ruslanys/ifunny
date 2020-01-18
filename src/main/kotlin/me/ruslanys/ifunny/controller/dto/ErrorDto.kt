package me.ruslanys.ifunny.controller.dto

import org.springframework.http.HttpStatus
import java.util.*

data class ErrorDto(
        val path: String,
        val status: Int,
        val error: String,
        val message: String?,
        val timestamp: Date = Date()
) {
    constructor(path: String, httpStatus: HttpStatus, message: String?) : this(
            path,
            httpStatus.value(),
            httpStatus.reasonPhrase,
            message
    )
}

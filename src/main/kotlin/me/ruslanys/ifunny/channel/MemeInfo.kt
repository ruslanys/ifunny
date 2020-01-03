package me.ruslanys.ifunny.channel

import java.time.LocalDateTime

data class MemeInfo(
        val pageUrl: String? = null,
        val originUrl: String? = null,
        val title: String? = null,
        val publishDateTime: LocalDateTime? = null,
        val likes: Int? = null,
        val comments: Int? = null,
        val author: String? = null
)

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
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemeInfo

        if (pageUrl != other.pageUrl) return false

        return true
    }

    override fun hashCode(): Int {
        return pageUrl?.hashCode() ?: 0
    }

}

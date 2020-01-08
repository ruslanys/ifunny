package me.ruslanys.ifunny.controller.dto

import me.ruslanys.ifunny.domain.Meme
import java.time.LocalDateTime

data class MemeDto(
        val id: String,
        val language: String,
        val channelName: String,
        val title: String,
        val url: String,
        val contentType: String,
        val publishDateTime: LocalDateTime?,
        val author: String?,
        val likes: Int?,
        val comments: Int?
) {
    constructor(meme: Meme) : this(
            meme.id.toHexString(),
            meme.language,
            meme.channelName,
            meme.title,
            "https://${meme.file.bucket}.s3.${meme.file.region}.amazonaws.com/${meme.file.name}",
            meme.file.contentType,
            meme.publishDateTime,
            meme.author,
            meme.likes,
            meme.comments
    )
}

package me.ruslanys.ifunny.util

import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom

fun createDummyMeme(
        language: Language = Language.GERMAN,
        channelName: String = "TestChannel",
        pageUrl: String = UUID.randomUUID().toString(),
        title: String = "Title",
        resourceUrl: String = "resourceUrl",
        publishDateTime: LocalDateTime? = LocalDateTime.now(),
        author: String = "ruslanys",
        likes: Int = ThreadLocalRandom.current().nextInt(1, 100),
        comments: Int = ThreadLocalRandom.current().nextInt(1, 100)
) = Meme(language.code, channelName, pageUrl, title, resourceUrl, publishDateTime, author, likes, comments)

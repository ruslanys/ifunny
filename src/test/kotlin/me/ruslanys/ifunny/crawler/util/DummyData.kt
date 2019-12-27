package me.ruslanys.ifunny.crawler.util

import me.ruslanys.ifunny.crawler.domain.Language
import me.ruslanys.ifunny.crawler.domain.Meme
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom

fun createDummyMeme(
        language: Language = Language.GERMAN,
        sourceName: String = "TestSource",
        pageUrl: String = UUID.randomUUID().toString(),
        title: String = "Title",
        resourceUrl: String = "resourceUrl",
        publishDateTime: LocalDateTime? = LocalDateTime.now(),
        author: String = "ruslanys",
        likes: Int = ThreadLocalRandom.current().nextInt(1, 100),
        comments: Int = ThreadLocalRandom.current().nextInt(1, 100)
) = Meme(language.code, sourceName, pageUrl, title, resourceUrl, publishDateTime, author, likes, comments)

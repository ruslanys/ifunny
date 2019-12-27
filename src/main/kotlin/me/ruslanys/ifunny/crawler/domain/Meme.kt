package me.ruslanys.ifunny.crawler.domain

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime

@Document
data class Meme(

        @Indexed
        val language: String,
        val sourceName: String,

        @Indexed(unique = true)
        val pageUrl: String,
        val title: String,

        val resourceUrl: String,

        val publishDateTime: LocalDateTime? = null,
        val author: String? = null,
        val likes: Int? = null,
        val comments: Int? = null,

        @MongoId
        val id: ObjectId = ObjectId()

)

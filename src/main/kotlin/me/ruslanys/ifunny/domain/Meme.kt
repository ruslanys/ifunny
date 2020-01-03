package me.ruslanys.ifunny.domain

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime

@Document
@CompoundIndex(name = "language_fingerprint_idx", def = "{'language': 1, 'fingerprint': 1}")
data class Meme(

        @Indexed
        val language: String,
        val channelName: String,

        @Indexed(unique = true)
        val pageUrl: String,
        val title: String,

        val originUrl: String,
        val file: S3File,

        val fingerprint: String? = null,

        val publishDateTime: LocalDateTime? = null,
        val author: String? = null,
        val likes: Int? = null,
        val comments: Int? = null,

        @MongoId
        val id: ObjectId = ObjectId()

)

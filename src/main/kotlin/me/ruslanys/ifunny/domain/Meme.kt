package me.ruslanys.ifunny.domain

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime

@Document
@CompoundIndexes(
        CompoundIndex(name = "language2fingerprint_idx", def = "{'language': 1, 'fingerprint': 1}"),
        CompoundIndex(name = "language2publishDateTime_idx", def = "{'language': 1, 'publishDateTime': -1}"),
        CompoundIndex(name = "language2likes_idx", def = "{'language': 1, 'likes': -1}")
)
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

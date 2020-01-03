package me.ruslanys.ifunny.domain

data class S3File(
        val region: String,
        val bucket: String,
        val name: String,
        val contentType: String,
        val checksum: String,
        val size: Long
)

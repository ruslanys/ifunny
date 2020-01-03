package me.ruslanys.ifunny.service

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.domain.S3File

interface MemeService {

    fun add(channel: Channel, info: MemeInfo, file: S3File, fingerprint: String?): Meme

    fun isExists(language: Language, fingerprint: String): Boolean

    fun findByPageUrls(urls: List<String>): List<Meme>

}

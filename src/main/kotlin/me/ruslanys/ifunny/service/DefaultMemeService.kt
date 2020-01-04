package me.ruslanys.ifunny.service

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.repository.MemeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DefaultMemeService(private val memeRepository: MemeRepository) : MemeService {

    override fun add(channel: Channel, info: MemeInfo, file: S3File, fingerprint: String?): Meme {
        info.pageUrl ?: throw IllegalArgumentException("A meme page URL can't be null!")
        info.title ?: throw IllegalArgumentException("A meme title can't be null!")
        info.originUrl ?: throw IllegalArgumentException("A meme origin URL can't be null!")

        val meme = Meme(channel.language.code, channel.getName(), info.pageUrl, info.title, info.originUrl, file,
                fingerprint, info.publishDateTime, info.author, info.likes, info.comments)
        return memeRepository.save(meme)
    }

    override fun isExists(language: Language, fingerprint: String): Boolean =
            memeRepository.existsByLanguageAndFingerprint(language.code, fingerprint)

    override fun findByPageUrls(urls: List<String>): List<Meme> = memeRepository.findByPageUrlIn(urls)

    override fun getPage(language: Language, pageRequest: Pageable): Page<Meme> {
        return memeRepository.findByLanguage(language.code, pageRequest)
    }

    override fun getById(id: String): Meme =
            memeRepository.findById(id).orElseThrow { NotFoundException("Meme with ID $id not found.") }

}

package me.ruslanys.ifunny.service

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.repository.MemeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DefaultMemeService(private val memeRepository: MemeRepository) : MemeService {

    override suspend fun add(channel: Channel, info: MemeInfo, file: S3File, fingerprint: String?): Meme {
        info.pageUrl ?: throw IllegalArgumentException("A meme page URL can't be null!")
        info.title ?: throw IllegalArgumentException("A meme title can't be null!")
        info.originUrl ?: throw IllegalArgumentException("A meme origin URL can't be null!")

        val meme = Meme(channel.language.code, channel.getName(), info.pageUrl, info.title, info.originUrl, file,
                fingerprint, info.publishDateTime, info.author, info.likes, info.comments)
        return memeRepository.save(meme).awaitSingle()
    }

    override suspend fun isExists(language: Language, fingerprint: String): Boolean {
        return memeRepository.existsByLanguageAndFingerprint(language.code, fingerprint).awaitSingle()
    }

    override suspend fun findByPageUrls(urls: List<String>): List<Meme> {
        return memeRepository.findByPageUrlIn(urls).asFlow().toList()
    }

    override suspend fun getPage(language: Language, pageRequest: Pageable): Page<Meme> = coroutineScope {
        val memes = async {
            memeRepository.findByLanguage(language.code, pageRequest).asFlow().toList()
        }
        val count = async {
            memeRepository.countByLanguage(language.code).awaitSingle()
        }

        PageImpl<Meme>(memes.await(), pageRequest, count.await())
    }

    override suspend fun getById(id: String): Meme = memeRepository.findById(id).awaitFirstOrElse {
        throw NotFoundException("Meme with ID $id not found.")
    }

}

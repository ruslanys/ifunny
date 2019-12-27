package me.ruslanys.ifunny.crawler.service

import me.ruslanys.ifunny.crawler.domain.Meme
import me.ruslanys.ifunny.crawler.repository.MemeRepository
import me.ruslanys.ifunny.crawler.source.MemeInfo
import me.ruslanys.ifunny.crawler.source.Source
import org.springframework.stereotype.Service


@Service
class DefaultMemeService(private val memeRepository: MemeRepository) : MemeService {

    override fun add(source: Source, info: MemeInfo): Meme {
        val meme = Meme(
                source.language.code,
                source.javaClass.simpleName,
                info.pageUrl!!,
                info.title!!,
                info.resourceUrl!!,
                info.publishDateTime,
                info.author,
                info.likes,
                info.comments
        )

        return memeRepository.save(meme)
    }


}

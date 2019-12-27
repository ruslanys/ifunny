package me.ruslanys.ifunny.crawler.service

import me.ruslanys.ifunny.crawler.domain.Meme
import me.ruslanys.ifunny.crawler.source.MemeInfo
import me.ruslanys.ifunny.crawler.source.Source

interface MemeService {

    fun add(source: Source, info: MemeInfo): Meme

}

package me.ruslanys.ifunny.crawler.source

import me.ruslanys.ifunny.crawler.domain.Language

abstract class Source(val language: Language, val baseUrl: String) {

    /**
     * The method returns page URL based on the page number.
     */
    abstract fun pagePath(pageNumber: Int): String

    /**
     * The method gets listing page body and returns grabbed memes list.
     */
    abstract fun parsePageList(body: String): List<MemeInfo>

    /**
     * The method gets meme page and returns meme extended information.
     */
    abstract fun parsePageMeme(info: MemeInfo, body: String): MemeInfo

}

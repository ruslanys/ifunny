package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language

/**
 * Channel itself is a source of memes.
 * To add a new channel a developer should extend this class and declare the implementation as a Spring [Bean][org.springframework.context.annotation.Bean].
 *
 * Each channel should implement three declared methods: [pagePath], [parsePage], [parseMeme].
 *
 * Architecture based on the assumption, that each channel returns memes in a pageable format (divided by pages aka Pagination).
 *
 * In that case grab process is the following:
 * 1. Getting a page URL for a specific page number by [pagePath].
 * 1. Downloading the page content and parse it by [parsePage]. As a result, a channel should return a list of memes.
 * All fields are optional, except `pageUrl` field as each website is very specific.
 * 1. Per each meme individual page (`pageUrl` parameter) downloading page content and parse it by [parseMeme] returning all possible to fetch information.
 */
abstract class Channel(val language: Language, val baseUrl: String) {

    /**
     * The method returns page URL based on the page number.
     */
    abstract suspend fun pagePath(pageNumber: Int): String

    /**
     * The method gets Page body and returns grabbed memes list.
     */
    abstract suspend fun parsePage(pageNumber: Int, body: String): Page

    /**
     * The method gets meme individual page and returns extended information.
     */
    abstract suspend fun parseMeme(info: MemeInfo, body: String): MemeInfo

    fun getName(): String = javaClass.simpleName

    override fun toString(): String {
        return getName()
    }

}

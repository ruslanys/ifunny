package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LachschonChannel : Channel(Language.GERMAN, "https://www.lachschon.de") {

    override fun pagePath(pageNumber: Int): String {
        return "$baseUrl/?set_gallery_type=image&page=$pageNumber"
    }

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val boxes = document.select("#itemlist > li")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            val header = parseHeader(box)
            val url = header.first
            val title = header.second

            val author = parseAuthor(box)
            val likes = parseLikes(box)
            val comments = parseComments(box)

            val info = MemeInfo(
                    pageUrl = url,
                    title = title,
                    author = author,
                    likes = likes,
                    comments = comments
            )
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.selectFirst("a.title")
        val memeUrl = link.absUrl("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseAuthor(box: Element): String {
        val element = box.select("a.username").firstOrNull() ?:
                box.select("span.username_guest").first()

        return element.text()
    }

    private fun parseLikes(box: Element): Int {
        return box.selectFirst("a.favs").text().toInt()
    }

    private fun parseComments(box: Element): Int {
        return box.selectFirst("a.comments").text().toInt()
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("a.forward").isNotEmpty()
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val box = document.getElementById("main_content")

        val resourceUrl = parseResourceUrl(box)
        val publishDateTime = parsePublishDateTime(box)

        return MemeInfo(
                pageUrl = info.pageUrl,
                originUrl = resourceUrl,
                title = info.title,
                publishDateTime = publishDateTime,
                likes = info.likes,
                comments = info.comments,
                author = info.author
        )
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime {
        val dateStr = box.select("span.created-at").text()
        return LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER)
    }

    private fun parseResourceUrl(box: Element): String {
        return box.selectFirst("img.item_view").absUrl("src")
    }

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy',' HH:mm:ss")
    }

}

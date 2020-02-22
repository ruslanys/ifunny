package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Component
class OrschlurchChannel : Channel(Language.GERMAN, "https://de.orschlurch.net") {

    override suspend fun pagePath(pageNumber: Int): String {
        return if (pageNumber == 1) {
            "$baseUrl/area/videos"
        } else {
            "$baseUrl/area/videos/seite/$pageNumber"
        }
    }

    override suspend fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val boxes = document.getElementsByClass("portfolio-item")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            val type = parseType(box)
            if (type != "Videos") {
                continue // skip not videos
            }

            val (url, title) = parseHeader(box)
            val likes = parseLikes(box)
            val comments = parseComments(box)

            // --
            val info = MemeInfo(pageUrl = url, title = title, likes = likes, comments = comments)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseType(box: Element): String {
        return box.select(".card-cat > a").text()
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.selectFirst(".card-title > a")
        val url = link.absUrl("href")
        val title = link.text()

        return url to title
    }

    private fun parseLikes(box: Element): Int {
        return box.selectFirst(".card-meta .like").text().toInt()
    }

    private fun parseComments(box: Element): Int {
        return box.select(".card-meta span").last().text().toInt()
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select(".pagination").text().contains("›")
    }

    override suspend fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }

        // --
        val publishDateTime = parsePublishDateTime(document)
        val author = parseAuthor(document)
        val resourceUrl = parseVideoUrl(document)

        // --
        return MemeInfo(info.pageUrl, resourceUrl, info.title, publishDateTime, info.likes, info.comments, author)
    }

    private fun parsePublishDateTime(document: Element): LocalDateTime {
        val meta = document.selectFirst("meta[property=article:published_time]").attr("content")
        return ZonedDateTime.parse(meta).toLocalDateTime()
    }

    private fun parseAuthor(document: Element): String {
        return document.selectFirst(".uploader a").text()
    }

    private fun parseVideoUrl(document: Element): String {
        return document.selectFirst(".container > video > source").absUrl("src")
    }

}

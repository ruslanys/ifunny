package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class FunpotChannel : Channel(Language.GERMAN, "https://funpot.net") {

    override suspend fun pagePath(pageNumber: Int): String = "$baseUrl/entdecken/lustiges/$pageNumber/"

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body)
        val boxes = document.getElementsByClass("contentline")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            // Type
            val type = parseType(box)
            if (type == null || !type.startsWith("Bild") && !type.startsWith("Online-Video")) {
                continue // skip non-image and non-video content
            }

            // Header
            val (url, title) = parseHeader(box)

            // Likes
            val likes = parseLikes(box)

            // Author
            val author = parseAuthor(box)

            // Publish Date
            val publishDateTime = parsePublishDateTime(box)

            // --
            val info = MemeInfo(
                    pageUrl = url,
                    title = title,
                    likes = likes,
                    author = author,
                    publishDateTime = publishDateTime
            )
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseType(box: Element): String? {
        return box.select(".look_datei > .kleine_schrift").firstOrNull()?.text()
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.select(".look_datei > a").first()

        val memeUrl = link.attr("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseLikes(box: Element): Int {
        return box.select("td.look_bewertung .kleine_schrift").firstOrNull()?.text()?.toInt() ?: 0
    }

    private fun parseAuthor(box: Element): String? {
        return box.select("td.look_nickname a").firstOrNull()?.text()
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime {
        val dateText = box.select("td.look_freigabedatum").text()

        // Date unification
        val now = LocalDateTime.now()
        val yearString = now.year.toString().substring(2)

        val unifiedDateText = dateText
                .replace("heute", "${now.dayOfMonth}.${now.monthValue}.")
                .replace(".-", ".$yearString-")

        // --
        return LocalDateTime.parse(unifiedDateText, DATE_EXTRACTOR)
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("a > img[src=https://funpot.net/includes/logos/pfeil_rechts.gif]").isNotEmpty()
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val container = document.getElementById("content")

        // --
        val resourceUrl = parseResourceUrl(container)

        // --
        return MemeInfo(
                pageUrl = info.pageUrl,
                originUrl = resourceUrl,
                title = info.title,
                likes = info.likes,
                author = info.author,
                publishDateTime = info.publishDateTime
        )
    }

    private fun parseResourceUrl(container: Element): String {
        return container.getElementById("Direktdownload")?.absUrl("href")
                ?: container.select("video > source").first().absUrl("src")
    }

    companion object {
        private val DATE_EXTRACTOR = DateTimeFormatter.ofPattern("d.M.yy'-'HH:mm")
    }

}

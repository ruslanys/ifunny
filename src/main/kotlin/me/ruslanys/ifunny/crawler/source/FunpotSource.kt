package me.ruslanys.ifunny.crawler.source

import me.ruslanys.ifunny.crawler.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class FunpotSource : Source(Language.GERMAN, "https://funpot.net") {

    override fun pagePath(pageNumber: Int): String = "$baseUrl/entdecken/lustiges/$pageNumber/"

    override fun parsePageList(body: String): List<MemeInfo> {
        val document = Jsoup.parse(body)
        val boxes = document.getElementsByClass("contentline")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            // Type
            val type = parseType(box)
            if (!type.startsWith("Bild") && !type.startsWith("Online-Video")) {
                continue // skip non-image and non-video content
            }

            // Header
            val header = parseHeader(box)
            val url = header.first
            val title = header.second

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

        return list
    }

    private fun parseType(box: Element): String {
        val span = box.select(".look_datei > .kleine_schrift").first()
        return span.text()
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

    override fun parsePageMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val container = document.getElementById("content")

        // --
        val pictureUrl = parsePictureUrl(container)
        val videoUrl = parseVideoUrl(container)

        // --
        return MemeInfo(
                pageUrl = info.pageUrl,
                title = info.title,
                likes = info.likes,
                comments = info.comments,
                author = info.author,
                publishDateTime = info.publishDateTime,
                resourceUrl = pictureUrl ?: videoUrl
        )
    }

    private fun parsePictureUrl(container: Element): String? {
        return null
    }

    private fun parseVideoUrl(container: Element): String? {
        return container.getElementById("robe_player")
                ?.getElementsByTag("video")?.first()
                ?.getElementsByTag("source")?.first()
                ?.absUrl("src")
    }

    companion object {
        private val DATE_EXTRACTOR = DateTimeFormatter.ofPattern("dd.MM.yy'-'HH:mm")
    }

}

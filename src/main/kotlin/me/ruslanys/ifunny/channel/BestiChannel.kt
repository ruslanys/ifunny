package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class BestiChannel : Channel(Language.ITALIAN, "https://besti.it") {

    override suspend fun pagePath(pageNumber: Int): String {
        return "$baseUrl/$pageNumber"
    }

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }
        val boxes = document.select(".box")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            val (url, title) = parseHeader(box)
            if (url == "#") { // skip adv box
                continue
            }

            // --
            val rate = parseRate(box)
            if (rate < 0) { // skip negative
                continue
            }

            // --
            val comments = parseComments(box)
            val author = parseAuthor(box)
            val publishDateTime = parsePublishDateTime(box)

            // --
            val info = MemeInfo(url, null, title, publishDateTime, rate, comments, author)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.selectFirst("h2 > a")
        val url = link.attr("href")
        val title = link.text()

        return url to title
    }

    private fun parseRate(box: Element): Int {
        val rateText = box.selectFirst(".rate").text()
        val rateMatcher = NUMBER_EXTRACTOR.matcher(rateText)
        return if (rateMatcher.find()) {
            rateMatcher.group(1).toInt()
        } else {
            throw IllegalStateException("Can't parse rate")
        }
    }

    private fun parseComments(box: Element): Int {
        val commentsText = box.select(".objectMeta > a").last().text()
        val commentsMatcher = NUMBER_EXTRACTOR.matcher(commentsText)
        return if (commentsMatcher.find()) {
            commentsMatcher.group(1).toInt()
        } else {
            throw IllegalStateException("Can't parse comments number")
        }
    }

    private fun parseAuthor(box: Element): String {
        return box.selectFirst(".objectMeta > a").text()
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime {
        val metaText = box.selectFirst(".objectMeta").text()
        val dateTimeMatcher = DATE_EXTRACTOR.matcher(metaText)
        return if (dateTimeMatcher.find()) {
            val dateStr = dateTimeMatcher.group(1)
            LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER)
        } else {
            throw IllegalStateException("Can't parse publishDateTime")
        }
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("li.next").firstOrNull()?.hasClass("disabled") == false
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }

        val pictureUrl = parsePictureUrl(document)
        val videoUrl = parseVideoUrl(document)

        val resourceUrl = pictureUrl ?: videoUrl

        return MemeInfo(info.pageUrl, resourceUrl, info.title, info.publishDateTime, info.likes, info.comments, info.author)
    }

    private fun parsePictureUrl(document: Element): String? {
        val image = document.selectFirst(".box-img > img") ?: return null
        return image.absUrl("src")
    }

    private fun parseVideoUrl(document: Element): String? {
        val video = document.selectFirst(".box-video > video > source") ?: return null
        return video.absUrl("src")
    }


    companion object {
        private val DATE_EXTRACTOR = Pattern.compile("Aggiunto: (.*) per")
        private val NUMBER_EXTRACTOR = Pattern.compile("\\(([\\d-+]+)\\)")

        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

}

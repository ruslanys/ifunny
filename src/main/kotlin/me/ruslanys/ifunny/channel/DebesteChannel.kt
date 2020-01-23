package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class DebesteChannel : Channel(Language.GERMAN, "http://debeste.de") {

    override suspend fun pagePath(pageNumber: Int): String = "$baseUrl/$pageNumber"

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body)
        val boxes = document.select(".box")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            // Skip boxes without content
            if (box.select(".objectWrapper").isEmpty()) {
                continue
            }

            // Header
            val (url, title) = parseHeader(box)

            if (url == "#") { // skip adv box
                continue
            }

            // Rate
            val rate = parseRate(box)
            if (rate != null && rate < 0) { // skip negative
                continue
            }

            // Comments
            val comments = parseComments(box)

            // --
            val info = MemeInfo(pageUrl = url, title = title, likes = rate, comments = comments)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.select("h2 > a")

        val memeUrl = link.attr("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseRate(box: Element): Int? {
        val rateText = box.selectFirst(".rate").text()
        val rateMatcher = NUMBER_EXTRACTOR.matcher(rateText)
        return if (rateMatcher.find()) {
            rateMatcher.group(1).toInt()
        } else {
            null
        }
    }

    private fun parseComments(box: Element): Int? {
        val commentsText = box.selectFirst(".objectMeta > a").text()
        val commentsMatcher = NUMBER_EXTRACTOR.matcher(commentsText)
        return if (commentsMatcher.find()) {
            commentsMatcher.group(1).toInt()
        } else {
            null
        }
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("li.next").firstOrNull()?.hasClass("disabled") == false
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val box = document.selectFirst(".box")

        // --
        val pictureUrl = parsePictureUrl(box)
        val videoUrl = parseVideoUrl(box)
        val author = parseAuthor(box)
        val publishDateTime = parsePublishDateTime(box)

        // --
        return MemeInfo(
                pageUrl = info.pageUrl,
                title = info.title,
                likes = info.likes,
                comments = info.comments,
                originUrl = pictureUrl ?: videoUrl,
                author = author,
                publishDateTime = publishDateTime
        )
    }

    private fun parsePictureUrl(box: Element): String? {
        val image = box.selectFirst(".box-img > img") ?: return null
        return image.absUrl("src")
    }

    private fun parseVideoUrl(box: Element): String? {
        val video = box.selectFirst(".box-video > video > source") ?: return null
        return video.absUrl("src")
    }

    private fun parseAuthor(box: Element): String? {
        val metaText = box.selectFirst(".objectMeta").text()
        val authorMatcher = AUTHOR_EXTRACTOR.matcher(metaText)
        return if (authorMatcher.find()) {
            authorMatcher.group(1)
        } else {
            null
        }
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime? {
        val metaText = box.selectFirst(".objectMeta").text()
        val dateTimeMatcher = DATE_EXTRACTOR.matcher(metaText)
        return if (dateTimeMatcher.find()) {
            val dateStr = dateTimeMatcher.group(1)
            LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER)
        } else {
            null
        }
    }

    companion object {
        private val NUMBER_EXTRACTOR = Pattern.compile("\\(([\\d-+]+)\\)")
        private val AUTHOR_EXTRACTOR = Pattern.compile("von (.*) \\| Homepage")
        private val DATE_EXTRACTOR = Pattern.compile("hinzugefügt: (.*) Kommentar")
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

}

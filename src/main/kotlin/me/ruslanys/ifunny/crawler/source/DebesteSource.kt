package me.ruslanys.ifunny.crawler.source

import me.ruslanys.ifunny.crawler.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class DebesteSource : Source(Language.GERMAN, "http://debeste.de") {

    override fun pagePath(pageNumber: Int): String = "$baseUrl/$pageNumber"

    override fun parsePageList(body: String): List<MemeInfo> {
        val document = Jsoup.parse(body)
        val elements = document.getElementsByClass("box")

        val list = arrayListOf<MemeInfo>()

        for (box in elements) {
            // Header
            val header = parseHeader(box)
            val url = header.first
            val title = header.second

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

        return list
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val header = box.getElementsByTag("h2").first()
        val link = header.getElementsByTag("a").first()

        val memeUrl = link.attr("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseRate(box: Element): Int? {
        val rateText = box.getElementsByClass("rate").first().text()
        val rateMatcher = NUMBER_EXTRACTOR.matcher(rateText)
        return if (rateMatcher.find()) {
            rateMatcher.group(1).toInt()
        } else {
            null
        }
    }

    private fun parseComments(box: Element): Int? {
        val commentsText = box.getElementsByClass("objectMeta").first().getElementsByTag("a").text()
        val commentsMatcher = NUMBER_EXTRACTOR.matcher(commentsText)
        return if (commentsMatcher.find()) {
            commentsMatcher.group(1).toInt()
        } else {
            null
        }
    }

    override fun parsePageMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val box = document.getElementsByClass("box").first()

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
                resourceUrl = pictureUrl ?: videoUrl,
                author = author,
                publishDateTime = publishDateTime
        )
    }

    private fun parsePictureUrl(box: Element): String? {
        val boxes = box.getElementsByClass("box-img")
        return if (boxes.isNotEmpty()) {
            boxes.first().getElementsByTag("img").first().absUrl("src")
        } else {
            null
        }
    }

    private fun parseVideoUrl(box: Element): String? {
        val boxes = box.getElementsByClass("box-video")
        return if (boxes.isNotEmpty()) {
            boxes.first()
                    .getElementsByTag("video").first()
                    .getElementsByTag("source").first()
                    .absUrl("src")
        } else {
            null
        }
    }

    private fun parseAuthor(box: Element): String? {
        val metaText = box.getElementsByClass("objectMeta").first().text()
        val authorMatcher = AUTHOR_EXTRACTOR.matcher(metaText)
        return if (authorMatcher.find()) {
            authorMatcher.group(1)
        } else {
            null
        }
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime? {
        val metaText = box.getElementsByClass("objectMeta").first().text()
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

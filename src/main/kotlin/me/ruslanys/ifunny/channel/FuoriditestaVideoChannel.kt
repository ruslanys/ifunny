package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

@Component
class FuoriditestaVideoChannel(private val webClient: WebClient) : Channel(Language.ITALIAN, "http://www.fuoriditesta.it") {

    override suspend fun pagePath(pageNumber: Int): String {
        return "$baseUrl/video-divertenti/index-$pageNumber.php"
    }

    override suspend fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }
        val boxes = document.select(".homevideo > .videodiv")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            val (url, title) = parseHeader(box) ?: continue

            // --
            val info = MemeInfo(pageUrl = url, title = title)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String>? {
        val link = box.selectFirst(".videotitle > p > a") ?: return null
        val url = link.absUrl("href")
        val title = link.text()

        return url to title
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select(".pagbloc").text().contains("»")
    }

    override suspend fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }

        val publishDateTime = parsePublishDate(document).atStartOfDay()
        val resourceUrl = parseVideoUrl(document)

        return MemeInfo(
                pageUrl = info.pageUrl,
                title = info.title,
                publishDateTime = publishDateTime,
                originUrl = resourceUrl
        )
    }

    private fun parsePublishDate(box: Element): LocalDate {
        val metaText = box.select(".content .related").text()
        val dateMatcher = DATE_EXTRACTOR.matcher(metaText)

        return if (dateMatcher.find()) {
            val dateStr = dateMatcher.group(1).toLowerCase()
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } else {
            throw IllegalStateException("Cannot parse publish date")
        }
    }

    private suspend fun parseVideoUrl(document: Element): String {
        val frameUrl = document.selectFirst(".container iframe").absUrl("src")
        val frameBody = webClient.get().uri(frameUrl).retrieve().awaitBody<String>()
        val frameDocument = Jsoup.parse(frameBody)
        return frameDocument.selectFirst("#my-video > source").attr("src")
    }

    companion object {
        private val DATE_EXTRACTOR = Pattern.compile("Video inserito il (.*) Condividi")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)
    }

}

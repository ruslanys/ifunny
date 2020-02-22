package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

@Component
class FuoriditestaImagesChannel : Channel(Language.ITALIAN, "http://www.fuoriditesta.it") {

    override suspend fun pagePath(pageNumber: Int): String {
        return "$baseUrl/immagini-divertenti/index-$pageNumber.php"
    }

    override suspend fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }
        val boxes = document.select(".homeimmagini > .immaginidiv")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            val (url, title) = parseHeader(box)
            val publishDateTime = parsePublishDate(box).atStartOfDay()

            // --
            val info = MemeInfo(pageUrl = url, title = title, publishDateTime = publishDateTime)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.selectFirst(".immaginititle > p > a")
        val url = link.absUrl("href")
        val title = link.text()

        return url to title
    }

    private fun parsePublishDate(box: Element): LocalDate {
        val metaText = box.select(".immaginititle").last().text()
        val dateMatcher = DATE_EXTRACTOR.matcher(metaText)

        return if (dateMatcher.find()) {
            val dateStr = dateMatcher.group(1).toLowerCase()
            LocalDate.parse(dateStr, DATE_FORMATTER)
        } else {
            throw IllegalStateException("Cannot parse publish date")
        }
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select(".pagbloc").text().contains("»")
    }

    override suspend fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }

        val resourceUrl = parsePictureUrl(document)

        return MemeInfo(
                pageUrl = info.pageUrl,
                title = info.title,
                publishDateTime = info.publishDateTime,
                originUrl = resourceUrl
        )
    }

    private fun parsePictureUrl(document: Element): String {
        return document.selectFirst(".container .imgcont img").absUrl("src")
    }

    companion object {
        private val DATE_EXTRACTOR = Pattern.compile("Inserita il (.*) su")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)
    }

}

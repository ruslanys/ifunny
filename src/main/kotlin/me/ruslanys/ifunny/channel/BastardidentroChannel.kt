package me.ruslanys.ifunny.channel

import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.net.URLDecoder
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Component
class BastardidentroChannel : Channel(Language.ITALIAN, "https://www.bastardidentro.it") {

    override suspend fun pagePath(pageNumber: Int): String {
        return if (pageNumber == 1) {
            "$baseUrl/immagini-e-vignette-divertenti"
        } else {
            "$baseUrl/immagini-e-vignette-divertenti?page=${pageNumber - 1}"
        }
    }

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }
        val boxes = document.select("#block-system-main .view-content > div.views-row-odd, #block-system-main .view-content > div.views-row-even")

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
        val link = box.selectFirst(".views-field-title a") ?: return null
        val url = link.absUrl("href")
        val decodedUrl = URLDecoder.decode(url, Charsets.UTF_8)
        val title = link.text()

        return decodedUrl to title
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select(".pagination > .next").isNotEmpty()
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).apply { setBaseUri(baseUrl) }

        val publishedDateTime = parsePublishDateTime(document)
        val resourceUrl = parsePictureUrl(document)

        return MemeInfo(
                pageUrl = info.pageUrl,
                title = info.title,
                publishDateTime = publishedDateTime,
                originUrl = resourceUrl
        )
    }

    private fun parsePublishDateTime(document: Element): LocalDateTime {
        val meta = document.selectFirst("meta[property=article:published_time]").attr("content")
        return ZonedDateTime.parse(meta).toLocalDateTime()
    }

    private fun parsePictureUrl(document: Element): String {
        return document.selectFirst("#internal .img-responsive").absUrl("src")
    }

}

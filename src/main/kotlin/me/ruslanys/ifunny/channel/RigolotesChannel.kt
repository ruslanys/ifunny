package me.ruslanys.ifunny.channel

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RigolotesChannel(
        redisTemplate: ReactiveRedisTemplate<String, Any>,
        private val webClient: WebClient
) : Channel(Language.FRENCH, "https://rigolotes.fr") {

    private val valueOperations: ReactiveValueOperations<String, Any> = redisTemplate.opsForValue()

    override suspend fun pagePath(pageNumber: Int): String {
        val pages = getPagesNumber() ?: fetchPagesNumber()
        val current = pages - (pageNumber - 1)

        return "$baseUrl/page/$current"
    }

    private suspend fun getPagesNumber(): Int? {
        return valueOperations[pagesKey()].awaitFirstOrNull() as Int?
    }

    private suspend fun fetchPagesNumber(): Int {
        val body = webClient.get().uri(baseUrl).retrieve().awaitBody<String>()
        val document = Jsoup.parse(body)
        val current = document.selectFirst("div.page-numbers > a.font-weight-bold").text()
        val pages = current.toInt()

        valueOperations.setIfAbsent(pagesKey(), pages, Duration.ofHours(1)).awaitSingle()

        return pages
    }

    private fun pagesKey() = "${getName()}:pages"

    override fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val boxes = document.select("div.articles-container > div.article-box")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            if (box.select("div.video-container > iframe").isNotEmpty()) {
                continue // skip youtube videos
            }

            val header = parseHeader(box)
            val url = header.first
            val title = header.second

            val votes = parseVotes(box)
            if (votes < 0) {
                continue // skip negative
            }

            val publishDateTime = parsePublishDateTime(box)
            val author = parseAuthor(box)

            val info = MemeInfo(
                    pageUrl = url,
                    title = title,
                    likes = votes,
                    publishDateTime = publishDateTime,
                    author = author
            )
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.selectFirst("h2 > a")

        val memeUrl = link.absUrl("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseVotes(box: Element): Int {
        return box.selectFirst("div.votes > span.upvotes").text()
                .replace("+", "") // remove plus sign
                .replace(" ", "") // remove spaces
                .toInt()
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime {
        val dateStr = box.select("div.info > span").last().text()
        return LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER)
    }

    private fun parseAuthor(box: Element): String {
        return box.select("div.info > a")[0].text()
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("div.pagination-menu div.next-button a").text().contains("Suivant")
    }

    override fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val box = document.selectFirst(".article-box")

        val videoUrl = parseVideoUrl(box)
        val pictureUrl = parsePictureUrl(box)

        return MemeInfo(
                pageUrl = info.pageUrl,
                originUrl = videoUrl ?: pictureUrl,
                title = info.title,
                publishDateTime = info.publishDateTime,
                likes = info.likes,
                author = info.author
        )
    }

    private fun parsePictureUrl(box: Element): String? {
        val image = box.selectFirst("div.center-block > a > img") ?: return null
        return image.absUrl("src")
    }

    private fun parseVideoUrl(box: Element): String? {
        val video = box.selectFirst("div.center-block > video > source") ?: return null
        return video.absUrl("src")
    }

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    }

}

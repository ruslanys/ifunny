package me.ruslanys.ifunny.channel

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import me.ruslanys.ifunny.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class YatahongaChannel(private val objectMapper: ObjectMapper) : Channel(Language.FRENCH, "https://www.yatahonga.com") {

    override suspend fun pagePath(pageNumber: Int): String {
        return if (pageNumber == 1) {
            "$baseUrl/nouveautes/"
        } else {
            "$baseUrl/nouveautes/p${pageNumber}/"
        }
    }

    override suspend fun parsePage(pageNumber: Int, body: String): Page {
        val document = Jsoup.parse(body)
        val articles = document.getElementsByTag("article")

        val list = arrayListOf<MemeInfo>()

        for (article in articles) {
            val (url, title) = parseHeader(article) ?: continue

            val points = parsePoints(article)
            if (points < 0) {
                continue // skip negative
            }

            val comments = parseComments(article)

            // --
            val info = MemeInfo(pageUrl = url, title = title, likes = points, comments = comments)
            list.add(info)
        }

        return Page(pageNumber, isHasNext(document), list)
    }

    private fun parseHeader(article: Element): Pair<String, String>? {
        val link = article.select("header a").firstOrNull() ?: return null
        val memeUrl = link.attr("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parsePoints(article: Element): Int {
        return article.select("p.post-meta > span.badge-item-love-count").text().toInt()
    }

    private fun parseComments(article: Element): Int {
        return article.select("p.post-meta > a.comment").text()
                .split(" ")[0]
                .toInt()
    }

    private fun isHasNext(document: Element): Boolean {
        return document.select("div.pagingbuttons > a.next").isNotEmpty()
    }

    override suspend fun parseMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body)
        val json = document.selectFirst("script[type=application/ld+json]").data()

        val post = objectMapper.readValue<BlogPost>(json, BlogPost::class.java)

        val author = post.author
        val publishDateTime = LocalDate.parse(post.datePublished, DATE_EXTRACTOR).atStartOfDay()
        val resourceUrl = if (post.video != null) {
            post.video.url
        } else {
            post.image.url
        }

        return MemeInfo(
                pageUrl = info.pageUrl,
                originUrl = resourceUrl,
                title = info.title,
                publishDateTime = publishDateTime,
                likes = info.likes,
                comments = info.comments,
                author = author
        )
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BlogPost(
            val headline: String,
            val author: String,
            val datePublished: String,
            val dateModified: String,
            val image: Object,
            val video: Object?
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Object(val url: String)
    }

    companion object {
        private val DATE_EXTRACTOR = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

}

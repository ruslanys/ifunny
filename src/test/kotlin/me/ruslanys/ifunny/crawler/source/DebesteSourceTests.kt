package me.ruslanys.ifunny.crawler.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DebesteSourceTests {

    private val source = DebesteSource()

    @Test
    fun firstPagePathTest() {
        val pagePath = source.pagePath(1)
        assertThat(pagePath).isEqualTo("http://debeste.de/1")
    }

    @Test
    fun hundredPagePathTest() {
        val pagePath = source.pagePath(100)
        assertThat(pagePath).isEqualTo("http://debeste.de/100")
    }

    @Test
    fun parseProperListPageShouldReturnList() {
        val html = javaClass.getResourceAsStream("debeste/list.html").bufferedReader().use {
            it.readText()
        }

        // --
        val list = source.parsePageList(html)

        // --
        assertThat(list).hasSize(8)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list.map { it.likes }).containsExactly(5, 30, 29, 202, 43, 47, 57, 19)
        assertThat(list.map { it.comments }).containsExactly(1, 0, 0, 5, 0, 6, 0, 0)
    }

    @Test
    fun parseInvalidListPageShouldReturnEmptyList() {
        // --
        val list = source.parsePageList("<html></html>")

        // --
        assertThat(list).hasSize(0)
    }

    @Test
    fun parsePicturePageWithAuthorAndDate() {
        val baseInfo = MemeInfo(
                pageUrl = "http://debeste.de/109224/M-nner-Vor-dem-Rasieren-Nach-dem-Rasieren",
                title = "Männer... Vor dem Rasieren... Nach dem Rasieren..",
                likes = 24,
                comments = 0
        )
        val html = javaClass.getResourceAsStream("debeste/page_picture_AuthorDate.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.resourceUrl).isEqualTo("http://debeste.de/upload/f372e8943cf093f1beb344f95a419daa6585.jpg")
        assertThat(info.author).isEqualTo("heidi2")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2019, 12, 26, 13, 40, 1))
    }

    @Test
    fun parsePicturePageWithAuthor() {
        val baseInfo = MemeInfo(
                pageUrl = "http://debeste.de/91020/Jahrelang-haben-wir-uns-ber-diejenigen-lustig-gemacht",
                title = "Jahrelang haben wir uns über diejenigen lustig gemacht..",
                likes = 93,
                comments = 0
        )
        val html = javaClass.getResourceAsStream("debeste/page_picture_Author.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.resourceUrl).isEqualTo("http://debeste.de/upload/e4b9c282887d58b5ecfcc7d02823d4e4.jpg")
        assertThat(info.author).isEqualTo("pola")

        assertThat(info.publishDateTime).isNull()
    }

    @Test
    fun parseGifPageWithDate() {
        val baseInfo = MemeInfo(
                pageUrl = "http://debeste.de/108960/Besonderer-Weihnachtsbaum",
                title = "Besonderer Weihnachtsbaum..",
                likes = 60,
                comments = 0
        )
        val html = javaClass.getResourceAsStream("debeste/page_gif_Date.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.resourceUrl).isEqualTo("http://debeste.de/upload/c8b848656368e728b70647b2d8cb3ca76476.gif")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2019, 12, 24, 16, 40, 1))

        assertThat(info.author).isNull()
    }

    @Test
    fun parseVideoPageWithComments() {
        val baseInfo = MemeInfo(
                pageUrl = "http://debeste.de/104767/Gibt-es-sofort-zu-Welcher-von-euch-war-es",
                title = "Gibt es sofort zu! Welcher von euch war es?",
                likes = 37,
                comments = 2
        )
        val html = javaClass.getResourceAsStream("debeste/page_video_Comments.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.resourceUrl).isEqualTo("http://debeste.de/upload2/v/cc2809a58b6e11c6c99ed9a362b18e822390.mp4")

        assertThat(info.author).isNull()
        assertThat(info.publishDateTime).isNull()
    }

}

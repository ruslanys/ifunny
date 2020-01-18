package me.ruslanys.ifunny.channel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class YatahongaChannelTests {

    private val channel = YatahongaChannel(jacksonObjectMapper())

    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://www.yatahonga.com/nouveautes/")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://www.yatahonga.com/nouveautes/p100/")
    }

    @Test
    fun parseProperPageShouldReturnList() {
        val html = javaClass.getResourceAsStream("yatahonga/page.html").bufferedReader().use {
            it.readText()
        }

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(20)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.likes != 0 }
        assertThat(list).allMatch { it.comments != 0 }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() {
        val html = javaClass.getResourceAsStream("yatahonga/page_last.html").bufferedReader().use {
            it.readText()
        }

        // --
        val page = channel.parsePage(1, html)

        // --
        assertThat(page.hasNext).isFalse()
    }

    @Test
    fun parseInvalidPageShouldReturnEmptyList() {
        val page = channel.parsePage(1, "<html></html>")
        assertThat(page.hasNext).isFalse()
        assertThat(page.memesInfo).isEmpty()
    }

    @Test
    fun parsePictureMeme() {
        val baseInfo = MemeInfo(
                pageUrl = "https://www.yatahonga.com/actualites/768nq/",
                title = "La grève SNCF commence à se voir",
                likes = 223,
                comments = 8
        )
        val html = javaClass.getResourceAsStream("yatahonga/meme_picture.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.originUrl).isEqualTo("https://www.yatahonga.com/data/media/1/202010/5e13966a8c614.jpg")
        assertThat(info.author).isEqualTo("ROUGETNOIRS")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2020, 1, 6, 0, 0))
    }

    @Test
    fun parseVideoMeme() {
        val baseInfo = MemeInfo(
                pageUrl = "https://www.yatahonga.com/gif/c78nq/",
                title = "Batman",
                likes = 4,
                comments = 2
        )
        val html = javaClass.getResourceAsStream("yatahonga/meme_video.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.originUrl).isEqualTo("https://www.yatahonga.com/data/media/11/202010/5e16bbcb751a3.mp4")
        assertThat(info.author).isEqualTo("sawubona94")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2020, 1, 9, 0, 0))
    }

}

package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OrschlurchChannelTests {

    private val channel = OrschlurchChannel()


    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://de.orschlurch.net/area/videos")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://de.orschlurch.net/area/videos/seite/100")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<OrschlurchChannelTests>("orschlurch/page.html")

        // --
        val page = channel.parsePage(33, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(17)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.likes != null }
        assertThat(list).allMatch { it.comments != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<OrschlurchChannelTests>("orschlurch/page_last.html")

        // --
        val page = channel.parsePage(60, html)

        // --
        assertThat(page.hasNext).isFalse()
    }

    @Test
    fun parseInvalidPageShouldReturnEmptyList() = runBlocking {
        val page = channel.parsePage(1, "<html></html>")
        assertThat(page.hasNext).isFalse()
        assertThat(page.memesInfo).isEmpty()
    }

    @Test
    fun parseVideoMeme() = runBlocking<Unit> {
        val baseInfo = MemeInfo(
                pageUrl = "https://de.orschlurch.net/post/der-etwas-andere-hochzeitskorso",
                title = "Der etwas andere Hochzeitskorso",
                likes = 1,
                comments = 0
        )
        val html = readResource<OrschlurchChannelTests>("orschlurch/meme_video.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.originUrl).isEqualTo("https://static.orschlurch.net/videos/2090/667139c1f82508e0.mp4")
        assertThat(info.author).isEqualTo("Admin")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2019, 7, 9, 19, 17))
    }

}

package me.ruslanys.ifunny.channel

import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.mockGet
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime

class FuoriditestaVideoChannelTests {

    private val webClient: WebClient = mock()
    private val channel = FuoriditestaVideoChannel(webClient)


    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("http://www.fuoriditesta.it/video-divertenti/index-1.php")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("http://www.fuoriditesta.it/video-divertenti/index-100.php")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<FuoriditestaVideoChannelTests>("fuoriditesta-video/page.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(15)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<FuoriditestaVideoChannelTests>("fuoriditesta-video/page_last.html")

        // --
        val page = channel.parsePage(221, html)

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
                pageUrl = "http://www.fuoriditesta.it/video-divertenti/cavallo-della-bambina-decide-di-giocare-nel-fango.html",
                title = "Il cavallo della bambina decide di giocare nel fango"
        )
        val html = readResource<FuoriditestaVideoChannelTests>("fuoriditesta-video/meme_video.html")
        val frameHtml = readResource<FuoriditestaVideoChannelTests>("fuoriditesta-video/frame.html")

        mockGet(webClient, "http://www.fuoriditesta.it/video-divertenti/embed.php?id=3329", frameHtml)

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)

        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2018, 5, 28, 0, 0))
        assertThat(info.originUrl).isEqualTo("http://www.fuoriditesta.it/umorismo/files/cavallo-decide-di-giocare-nel-fango.mp4")
    }

}

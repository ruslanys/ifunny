package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FuoriditestaImagesChannelTests {

    private val channel = FuoriditestaImagesChannel()


    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("http://www.fuoriditesta.it/immagini-divertenti/index-1.php")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("http://www.fuoriditesta.it/immagini-divertenti/index-100.php")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<FuoriditestaImagesChannelTests>("fuoriditesta-images/page.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(18)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.publishDateTime != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<FuoriditestaImagesChannelTests>("fuoriditesta-images/page_last.html")

        // --
        val page = channel.parsePage(216, html)

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
    fun parsePictureMeme() = runBlocking<Unit> {
        val baseInfo = MemeInfo(
                pageUrl = "http://www.fuoriditesta.it/immagini-divertenti/calzini-di-trump.html",
                title = "Calzini di Trump",
                publishDateTime = LocalDateTime.of(2019, 8, 26, 0, 0)
        )
        val html = readResource<FuoriditestaImagesChannelTests>("fuoriditesta-images/meme_picture.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("http://www.fuoriditesta.it/umorismo/immagini/divertenti/620x620xcalzini-di-trump.jpg.pagespeed.ic.44S2tn8xIE.jpg")
    }

}

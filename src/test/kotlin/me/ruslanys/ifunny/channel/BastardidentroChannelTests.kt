package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BastardidentroChannelTests {

    private val channel = BastardidentroChannel()


    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://www.bastardidentro.it/immagini-e-vignette-divertenti")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://www.bastardidentro.it/immagini-e-vignette-divertenti?page=99")
    }

    @Test
    fun parseProperPageShouldReturnList() {
        val html = javaClass.getResourceAsStream("bastardidentro/page.html").bufferedReader().use {
            it.readText()
        }

        // --
        val page = channel.parsePage(6, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(10)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() {
        val html = javaClass.getResourceAsStream("bastardidentro/page_last.html").bufferedReader().use {
            it.readText()
        }

        // --
        val page = channel.parsePage(1421, html)

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
                pageUrl = "https://www.bastardidentro.it/immagini-e-vignette-divertenti/inconveniente-nel-lavare-i-piatti-511606",
                title = "Inconveniente nel lavare i piatti!"
        )
        val html = javaClass.getResourceAsStream("bastardidentro/meme_picture.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)

        assertThat(info.originUrl).isEqualTo("https://www.bastardidentro.it/sites/default/files/styles/nullo/public/images/9/3/4/lavare-piatti.jpg?itok=hKaeHnDB")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2020, 2, 12, 19, 30))
    }

}

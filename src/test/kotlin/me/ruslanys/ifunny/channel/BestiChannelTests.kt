package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BestiChannelTests {

    private val channel = BestiChannel()


    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://besti.it/1")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://besti.it/100")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<BestiChannelTests>("besti/page.html")

        // --
        val page = channel.parsePage(2, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(9)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.likes != null }
        assertThat(list).allMatch { it.comments != null }
        assertThat(list).allMatch { it.author != null }
        assertThat(list).allMatch { it.publishDateTime != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<BestiChannelTests>("besti/page_last.html")

        // --
        val page = channel.parsePage(6590, html)

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
                "https://besti.it/74854/Io-alla-prova-costume-di-carnevale",
                null,
                "Io alla prova costume di carnevale..",
                LocalDateTime.of(2020, 2, 21, 5, 17, 44),
                15,
                0,
                "GiorgiaPi"
        )
        val html = readResource<BestiChannelTests>("besti/meme_picture.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://besti.it/upload/2519bab24510f843ab721f48c07a42992864.jpg")
    }

    @Test
    fun parseVideoMeme() = runBlocking<Unit> {
        val baseInfo = MemeInfo(
                "https://besti.it/74846/succede",
                null,
                "succede",
                LocalDateTime.of(2020, 2, 21, 1, 17, 43),
                6,
                1,
                "AronF"
        )
        val html = readResource<BestiChannelTests>("besti/meme_video.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://besti.it/upload2/v/22b4793a8b08fb9ad822d5c4262b690d9104.mp4")
    }

}

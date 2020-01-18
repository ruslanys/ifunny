package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FunpotChannelTests {

    private val channel = FunpotChannel()

    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://funpot.net/entdecken/lustiges/1/")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://funpot.net/entdecken/lustiges/100/")
    }

    @Test
    fun parseProperPageShouldReturnList() {
        val html = javaClass.getResourceAsStream("funpot/page.html").bufferedReader().use {
            it.readText()
        }

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(38)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list.map { it.likes }).containsExactly(0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0, 0, 0, 2, 3, 0, 3, 2, 1, 6, 1, 0, 0, 0, 0, 0, 0, 4, 0, 1, 1, 2, 0, 2, 1)
        assertThat(list.mapNotNull { it.author }).containsExactly("Nogula", "Fossy", "SueHoe", "Nogula", "SueHoe", "funmaster", "Alex", "Oweiowei", "Fossy", "Siggi", "Beatzekatze", "Beatzekatze", "Beatzekatze", "Beatzekatze", "Knatter", "Beatzekatze", "WienerWalzer", "Sokan", "Bert43", "Fossy", "Keule56", "Bonobo666", "Sokan", "Sylke")

        assertThat(list).allMatch { it.publishDateTime != null }
        assertThat(list.first().publishDateTime).isEqualTo(LocalDateTime.of(2020, 12, 26, 20, 51))
        assertThat(list.last().publishDateTime).isEqualTo(LocalDateTime.of(2020, 12, 26, 13, 53))
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() {
        val html = javaClass.getResourceAsStream("funpot/page_last.html").bufferedReader().use {
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
    fun parsePictureMemeTest() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454125&tagid=55&dateityp=",
                title = "Helmut Kohl kommt in die Hölle",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 20, 43),
                likes = 2,
                author = "Fossy"
        )
        val html = javaClass.getResourceAsStream("funpot/meme_picture.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://funpot.net/direktdownload/funpot125317829cbb65706700004545001keyzc4451y_x6c2/Helmut_Kohl_kommt_in_die_Hoelle.jpg")
    }

    @Test
    fun parseGifMemeTest() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454131&tagid=55&dateityp=",
                title = "Sportlich 291",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 20, 51),
                likes = 0
        )
        val html = javaClass.getResourceAsStream("funpot/meme_gif.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://funpot.net/direktdownload/funpot1312307933d068623100004548606keyz72451y_x4ba/Sportlich_291.gif")
    }

    @Test
    fun parseVideoMemeWithDirectLinkTest() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454085&tagid=55&dateityp=",
                title = "Wo ist er?",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 18, 13),
                likes = 2,
                author = "Oweiowei"
        )
        val html = javaClass.getResourceAsStream("funpot/meme_video_Direct.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://funpot.net/direktdownload/funpot085Vid868a36eo-qua0000454liMBkeyz03451y_x9cf/Wo_ist_er_.mp4")
    }

    @Test
    fun parseVideoMemeWithoutDirectLinkTest() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000455173&tagid=55&dateityp=#",
                title = "The Grüninen Profekt",
                publishDateTime = LocalDateTime.of(2019, 12, 30, 10, 43),
                likes = 0,
                author = "Carlos"
        )
        val html = javaClass.getResourceAsStream("funpot/meme_video.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.originUrl).isEqualTo("https://funpot.net/daten/key_xyz/90/funpot0000455173_SD.mp4")
    }

}

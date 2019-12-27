package me.ruslanys.ifunny.crawler.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class FunpotSourceTests {

    private val source = FunpotSource()

    @Test
    fun firstPagePathTest() {
        val pagePath = source.pagePath(1)
        assertThat(pagePath).isEqualTo("https://funpot.net/entdecken/lustiges/1/")
    }

    @Test
    fun hundredPagePathTest() {
        val pagePath = source.pagePath(100)
        assertThat(pagePath).isEqualTo("https://funpot.net/entdecken/lustiges/100/")
    }

    @Test
    fun parseProperListPageShouldReturnList() {
        val html = javaClass.getResourceAsStream("funpot/list.html").bufferedReader().use {
            it.readText()
        }

        // --
        val list = source.parsePageList(html)

        // --
        assertThat(list).hasSize(38)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list.map { it.likes }).containsExactly(0, 0, 0, 2, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0, 0, 0, 2, 3, 0, 3, 2, 1, 6, 1, 0, 0, 0, 0, 0, 0, 4, 0, 1, 1, 2, 0, 2, 1)
        assertThat(list.mapNotNull { it.author }).containsExactly("Nogula", "Fossy", "SueHoe", "Nogula", "SueHoe", "funmaster", "Alex", "Oweiowei", "Fossy", "Siggi", "Beatzekatze", "Beatzekatze", "Beatzekatze", "Beatzekatze", "Knatter", "Beatzekatze", "WienerWalzer", "Sokan", "Bert43", "Fossy", "Keule56", "Bonobo666", "Sokan", "Sylke")

        assertThat(list).allMatch { it.publishDateTime != null }
        assertThat(list.first().publishDateTime).isEqualTo(LocalDateTime.of(2019, 12, 26, 20, 51))
        assertThat(list.last().publishDateTime).isEqualTo(LocalDateTime.of(2019, 12, 26, 13, 53))
    }

    @Test
    fun parseInvalidListPageShouldReturnEmptyList() {
        // --
        val list = source.parsePageList("<html></html>")

        // --
        assertThat(list).hasSize(0)
    }

    @Test
    fun parsePicturePage() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454125&tagid=55&dateityp=",
                title = "Helmut Kohl kommt in die Hölle",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 20, 43),
                likes = 2,
                author = "Fossy"
        )
        val html = javaClass.getResourceAsStream("funpot/page_picture.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.resourceUrl).isEqualTo("https://funpot.net/direktdownload/funpot125317829cbb65706700004545001keyzc4451y_x6c2/Helmut_Kohl_kommt_in_die_Hoelle.jpg")
    }

    @Test
    fun parseGifPage() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454131&tagid=55&dateityp=",
                title = "Sportlich 291",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 20, 51),
                likes = 0
        )
        val html = javaClass.getResourceAsStream("funpot/page_gif.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.resourceUrl).isEqualTo("https://funpot.net/direktdownload/funpot1312307933d068623100004548606keyz72451y_x4ba/Sportlich_291.gif")
    }

    @Test
    fun parseVideoPage() {
        val baseInfo = MemeInfo(
                pageUrl = "https://funpot.net/?id=funpot0000454085&tagid=55&dateityp=",
                title = "Wo ist er?",
                publishDateTime = LocalDateTime.of(2019, 12, 26, 18, 13),
                likes = 2,
                author = "Oweiowei"
        )
        val html = javaClass.getResourceAsStream("funpot/page_video.html").bufferedReader().use {
            it.readText()
        }

        // --
        val info = source.parsePageMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)

        assertThat(info.resourceUrl).isEqualTo("https://funpot.net/direktdownload/funpot085Vid868a36eo-qua0000454liMBkeyz03451y_x9cf/Wo_ist_er_.mp4")
    }

}

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

}

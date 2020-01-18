package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexSuccessful
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyMeme
import me.ruslanys.ifunny.util.mockGet
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.client.WebClient


class PageIndexerTests {

    private val webClient: WebClient = mock()
    private val eventChannel: SendChannel<GrabEvent> = mock()
    private val memeService: MemeService = mock()

    private val pageIndexer: PageIndexer = PageIndexer(webClient, eventChannel, memeService)

    @Test
    fun pageIndexationShouldEmitMemesIndexationEvents() = runBlocking {
        val channel = DebesteChannel()
        val pageNumber = 100

        given(memeService.findByPageUrls(any())).willReturn(listOf())
        mockGet(webClient, channel.pagePath(pageNumber), ClassPathResource("debeste_page.html", PageIndexerTests::class.java))

        // --
        pageIndexer.handleEvent(PageIndexRequest(channel, pageNumber))

        // --
        verify(eventChannel, times(8)).send(any<MemeIndexRequest>())
        verify(eventChannel, times(1)).send(any<PageIndexSuccessful>())
    }

    @Test
    fun pageIndexationShouldSkipProcessedMemes() = runBlocking {
        val channel = DebesteChannel()
        val pageNumber = 100

        val urls = listOf(
                "http://debeste.de/109036/Ich-sp-re-die-Macht-in-mir-K-nnte-allerdings-auch",
                "http://debeste.de/109026/Beste-Freunde",
                "http://debeste.de/109180/Hast-du-immer-noch-Lust,-so-ein-K-tzchen-zu-streicheln"
        )
        val memes = urls.map { createDummyMeme(pageUrl = it) }

        given(memeService.findByPageUrls(any())).willReturn(memes)
        mockGet(webClient, channel.pagePath(pageNumber), ClassPathResource("debeste_page.html", PageIndexerTests::class.java))

        // --
        pageIndexer.handleEvent(PageIndexRequest(channel, pageNumber))

        // --
        verify(eventChannel, times(5)).send(any<MemeIndexRequest>())
        verify(eventChannel, times(1)).send(any<PageIndexSuccessful>())
    }

}

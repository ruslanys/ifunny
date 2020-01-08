package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import me.ruslanys.ifunny.Application
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.config.SpyConfig
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyMeme
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess


@RestClientTest(PageIndexer::class, GrabProperties::class)
@Import(SpyConfig::class, Application::class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD, classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PageIndexerTests {

    // @formatter:off
    @Autowired private lateinit var pageIndexer: PageIndexer
    @Autowired private lateinit var server: MockRestServiceServer

    @Autowired private lateinit var eventPublisher: ApplicationEventPublisher
    @MockBean private lateinit var memeService: MemeService
    // @formatter:on


    @Test
    fun pageIndexationShouldEmitMemesIndexationEvents() {
        val channel = DebesteChannel()
        val pageNumber = 100

        given(memeService.findByPageUrls(any())).willReturn(listOf())

        // --
        server.expect(requestTo(channel.pagePath(pageNumber))).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(ClassPathResource("debeste_page.html", javaClass), MediaType.TEXT_HTML))

        // --
        pageIndexer.indexPage(PageIndexRequest(channel, pageNumber))

        // --
        verify(eventPublisher, times(8)).publishEvent(any<MemeIndexRequest>())
        verify(eventPublisher, times(1)).publishEvent(any<PageIndexedEvent>())
    }

    @Test
    fun pageIndexationShouldSkipProcessedMemes() {
        val channel = DebesteChannel()
        val pageNumber = 100

        val urls = listOf("http://debeste.de/109036/Ich-sp-re-die-Macht-in-mir-K-nnte-allerdings-auch", "http://debeste.de/109026/Beste-Freunde", "http://debeste.de/109180/Hast-du-immer-noch-Lust,-so-ein-K-tzchen-zu-streicheln")
        val memes = urls.map { createDummyMeme(pageUrl = it) }

        given(memeService.findByPageUrls(any())).willReturn(memes)

        // --
        server.expect(requestTo(channel.pagePath(pageNumber))).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(ClassPathResource("debeste_page.html", javaClass), MediaType.TEXT_HTML))

        // --
        pageIndexer.indexPage(PageIndexRequest(channel, pageNumber))

        // --
        verify(eventPublisher, times(5)).publishEvent(any<MemeIndexRequest>())
        verify(eventPublisher, times(1)).publishEvent(any<PageIndexedEvent>())
    }

}

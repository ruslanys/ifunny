package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import me.ruslanys.ifunny.Application
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.config.SpyConfig
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.GrabProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
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


@RestClientTest(MemeIndexer::class, GrabProperties::class)
@Import(SpyConfig::class, Application::class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD, classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemeIndexerTests {

    // @formatter:off
    @Autowired private lateinit var memeIndexer: MemeIndexer
    @Autowired private lateinit var server: MockRestServiceServer

    @Autowired private lateinit var eventPublisher: ApplicationEventPublisher
    // @formatter:on

    @Test
    fun memeIndexationShouldEmitDownloadRequest() {
        val channel = DebesteChannel()
        val memeInfo = MemeInfo(pageUrl = "http://debeste.de/meme-123")

        server.expect(requestTo(memeInfo.pageUrl!!)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(ClassPathResource("debeste_meme.html", javaClass), MediaType.TEXT_HTML))

        // --
        memeIndexer.indexMeme(MemeIndexRequest(channel, memeInfo))

        // --
        val eventCaptor = argumentCaptor<ResourceDownloadRequest>()

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture())

        assertThat(eventCaptor.firstValue.info.pageUrl).isEqualTo(memeInfo.pageUrl)
        assertThat(eventCaptor.firstValue.info.originUrl).isEqualTo("http://debeste.de/upload/e4b9c282887d58b5ecfcc7d02823d4e4.jpg")
    }

}

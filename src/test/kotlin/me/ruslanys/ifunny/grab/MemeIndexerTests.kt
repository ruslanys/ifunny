package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.util.mockGet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.client.WebClient


class MemeIndexerTests {

    private val webClient: WebClient = mock()
    private val eventChannel: SendChannel<GrabEvent> = mock()

    private val memeIndexer = MemeIndexer(webClient, eventChannel)

    @Test
    fun memeIndexationShouldEmitDownloadRequest() = runBlocking<Unit> {
        val channel = DebesteChannel()
        val memeInfo = MemeInfo(pageUrl = "http://debeste.de/meme-123")

        mockGet(webClient, memeInfo.pageUrl!!, ClassPathResource("debeste_meme.html", MemeIndexer::class.java))

        // --
        memeIndexer.handleEvent(MemeIndexRequest(channel, memeInfo))

        // --
        val eventCaptor = argumentCaptor<ResourceDownloadRequest>()
        verify(eventChannel, times(1)).send(eventCaptor.capture())

        assertThat(eventCaptor.firstValue.info.pageUrl).isEqualTo(memeInfo.pageUrl)
        assertThat(eventCaptor.firstValue.info.originUrl).isEqualTo("http://debeste.de/upload/e4b9c282887d58b5ecfcc7d02823d4e4.jpg")
    }

}

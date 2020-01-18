package me.ruslanys.ifunny.grab

import kotlinx.coroutines.channels.SendChannel
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class MemeIndexer(
        private val webClient: WebClient,
        private val eventChannel: SendChannel<GrabEvent>
) : SuspendedEventListener<MemeIndexRequest> {

    /**
     * Indexation specific meme.
     */
    override suspend fun handleEvent(event: MemeIndexRequest) {
        val channel = event.channel
        val baseInfo = event.info

        // Fetch and parse page
        val responseBody = webClient.get().uri(baseInfo.pageUrl!!).retrieve().awaitBody<String>()
        val info = channel.parseMeme(baseInfo, responseBody)

        // Request resource download
        eventChannel.send(ResourceDownloadRequest(channel, info))
    }

    companion object {
        private val log = LoggerFactory.getLogger(MemeIndexer::class.java)
    }

}

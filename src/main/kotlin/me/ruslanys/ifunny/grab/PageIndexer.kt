package me.ruslanys.ifunny.grab

import kotlinx.coroutines.channels.SendChannel
import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexSuccessful
import me.ruslanys.ifunny.service.MemeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PageIndexer(
        private val webClient: WebClient,
        private val eventChannel: SendChannel<GrabEvent>,
        private val memeService: MemeService
) : SuspendedEventListener<PageIndexRequest> {

    /**
     * Indexation specific page.
     */
    override suspend fun handleEvent(event: PageIndexRequest) {
        val channel = event.channel
        val pageNumber = event.pageNumber

        // Fetch and parse Page
        val path = channel.pagePath(pageNumber)
        val responseBody = webClient.get().uri(path).retrieve().awaitBody<String>()
        val page = channel.parsePage(pageNumber, responseBody)

        // --
        val newMemes = memesIndexation(channel, page.memesInfo)

        // --
        eventChannel.send(PageIndexSuccessful(channel, page, newMemes))
    }

    /**
     * Memes indexation event.
     *
     * The method provides deduplication by filtering memes URLs with the existing in the DB list of URLs.
     */
    private suspend fun memesIndexation(channel: Channel, memesInfo: List<MemeInfo>): Int {
        val pageUrls = memesInfo.mapNotNull { it.pageUrl }
        val existingUrls = memeService.findByPageUrls(pageUrls).map { it.pageUrl }.toSet()
        val newMemes = memesInfo.filter { !existingUrls.contains(it.pageUrl) }.toSet()

        // Request memes indexation
        for (memeInfo in newMemes) {
            eventChannel.send(MemeIndexRequest(channel, memeInfo))
        }

        return newMemes.size
    }


    companion object {
        private val log = LoggerFactory.getLogger(PageIndexer::class.java)
    }

}

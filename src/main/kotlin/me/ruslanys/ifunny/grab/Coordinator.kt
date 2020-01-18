package me.ruslanys.ifunny.grab

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexSuccessful
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.repository.PageRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Coordinator(
        private val channels: List<Channel>,
        private val eventChannel: SendChannel<GrabEvent>,
        private val pageRepository: PageRepository,
        private val grabProperties: GrabProperties
) : SuspendedEventListener<PageIndexSuccessful> {

    @Scheduled(initialDelay = 10_000, fixedDelay = 3600_000)
    fun scheduleGrabbing() = GlobalScope.launch {
        for (channel in channels) {
            nextPageIndexRequest(channel)
        }
    }

    override suspend fun handleEvent(event: PageIndexSuccessful) {
        log.info("Page #{} from {} has been processed", event.page.number, event.channel.getName())

        val isChannelIndexed = pageRepository.getLast(event.channel).awaitFirstOrNull() != null

        if (!isChannelIndexed) {
            handleForNotIndexedChannel(event)
        } else {
            handleForIndexedChannel(event)
        }
    }

    /**
     * Next page indexation request.
     */
    private suspend fun nextPageIndexRequest(channel: Channel) {
        val pageNumber = pageRepository.incCurrent(channel).awaitSingle()
        eventChannel.send(PageIndexRequest(channel, pageNumber))
    }

    /**
     * Handle success page indexation event when the channel not fully indexed yet.
     */
    private suspend fun handleForNotIndexedChannel(event: PageIndexSuccessful) {
        val channel = event.channel

        if (event.page.hasNext) {
            // Keep grabbing until the end
            nextPageIndexRequest(channel)
        } else {
            // The end reached
            pageRepository.setLast(channel, event.page.number, grabProperties.retention.fullIndex).awaitSingle()
            pageRepository.clearCurrent(channel).awaitSingle()

            log.info("{} fully indexed.", channel.getName())
        }
    }

    /**
     * Handle success page indexation event when the channel already fully indexed.
     */
    private suspend fun handleForIndexedChannel(event: PageIndexSuccessful) {
        val channel = event.channel

        if (event.page.hasNext && event.new > 0) {
            nextPageIndexRequest(channel)
        } else {
            pageRepository.clearCurrent(channel).awaitSingle()

            log.info("{} new pages from {} indexed.", event.page.number, event.channel.getName())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Coordinator::class.java)
    }

}

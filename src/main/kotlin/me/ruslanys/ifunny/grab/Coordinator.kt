package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.repository.PageRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class Coordinator(
        private val channels: List<Channel>,
        private val eventPublisher: ApplicationEventPublisher,
        private val pageRepository: PageRepository,
        private val grabProperties: GrabProperties
) {

    @Scheduled(fixedRate = 3600_000)
    fun initializeGrabbing() {
        for (channel in channels) {
            nextPageIndexRequest(channel)
        }
    }

    @EventListener
    fun onIndexedPage(event: PageIndexedEvent) {
        log.info("Page #{} from {} has been processed", event.page.number, event.channel.getName())

        val isChannelIndexed = pageRepository.getLast(event.channel) != null

        if (!isChannelIndexed) {
            handleForNotIndexedChannel(event)
        } else {
            handleForIndexedChannel(event)
        }
    }

    /**
     * Next page indexation request.
     */
    private fun nextPageIndexRequest(channel: Channel) {
        val pageNumber = pageRepository.incCurrent(channel)
        eventPublisher.publishEvent(PageIndexRequest(channel, pageNumber))
    }

    /**
     * Handle success page indexation event when the channel not fully indexed yet.
     */
    private fun handleForNotIndexedChannel(event: PageIndexedEvent) {
        val channel = event.channel

        if (event.page.hasNext) {
            // Keep grabbing until the end
            nextPageIndexRequest(channel)
        } else {
            // The end reached
            pageRepository.setLast(channel, event.page.number, grabProperties.retention.fullIndex)
            pageRepository.clearCurrent(channel)

            log.info("{} fully indexed.", channel.getName())
        }
    }

    /**
     * Handle success page indexation event when the channel already fully indexed.
     */
    private fun handleForIndexedChannel(event: PageIndexedEvent) {
        val channel = event.channel

        if (event.page.hasNext && event.new > 0) {
            nextPageIndexRequest(channel)
        } else {
            pageRepository.clearCurrent(channel)

            log.info("{} new pages from {} indexed.", event.page.number, event.channel.getName())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Coordinator::class.java)
    }

}

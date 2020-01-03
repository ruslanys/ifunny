package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

// TODO
@Component
class GrabCoordinator(
        private val eventPublisher: ApplicationEventPublisher,
        private val channels: List<Channel>
) {

    /**
     * Initialize grabbing per each known [Channel].
     */
    @EventListener(ApplicationReadyEvent::class)
    fun initializeGrabbing() {
        for (channel in channels) {
            eventPublisher.publishEvent(PageIndexRequest(channel, 1))
        }
    }

    @EventListener
    fun onIndexedPage(event: PageIndexedEvent) {
        eventPublisher.publishEvent(PageIndexRequest(event.channel, event.pageNumber + 1))
    }

}

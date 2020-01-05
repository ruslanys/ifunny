package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import org.slf4j.LoggerFactory
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
        log.info("Page #{} from {} has been processed", event.page.number, event.channel.getName())

        if (event.page.hasNext) {
            eventPublisher.publishEvent(PageIndexRequest(event.channel, event.page.number + 1))
        } else {
            log.info("{} processed.", event.channel.getName())
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GrabCoordinator::class.java)
    }

}

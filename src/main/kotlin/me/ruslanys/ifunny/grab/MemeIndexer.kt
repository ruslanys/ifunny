package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.GrabProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MemeIndexer(
        restTemplateBuilder: RestTemplateBuilder,
        grabProperties: GrabProperties,
        private val eventPublisher: ApplicationEventPublisher
) {

    private val restTemplate = restTemplateBuilder
            .defaultHeader("User-Agent", grabProperties.userAgent)
            .build()

    /**
     * Indexation specific meme.
     */
    @Async
    @EventListener
    fun indexMeme(request: MemeIndexRequest) {
        val channel = request.channel
        val baseInfo = request.info

        log.trace("Request to index meme {}", baseInfo.pageUrl)

        // Fetch and parse page
        val responseBody = restTemplate.getForObject(baseInfo.pageUrl!!, String::class.java)
        val info = channel.parseMeme(baseInfo, responseBody!!)

        log.trace("Meme information {}", info)

        // Request resource download
        eventPublisher.publishEvent(ResourceDownloadRequest(channel, info))
    }


    companion object {
        private val log = LoggerFactory.getLogger(MemeIndexer::class.java)
    }

}

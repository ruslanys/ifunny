package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.service.MemeService
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PageIndexer(
        grabProperties: GrabProperties,
        restTemplateBuilder: RestTemplateBuilder,
        private val eventPublisher: ApplicationEventPublisher,
        private val memeService: MemeService
) {

    private val restTemplate = restTemplateBuilder
            .defaultHeader("User-Agent", grabProperties.userAgent)
            .build()

    /**
     * Indexation specific page.
     */
    @Async
    @EventListener
    fun indexPage(request: PageIndexRequest) {
        val channel = request.channel
        val pageNumber = request.pageNumber

        log.trace("Request to index page #{} for {}", pageNumber, channel.getName())

        // Fetch and parse Page
        val path = channel.pagePath(pageNumber)
        val responseBody = restTemplate.getForObject(path, String::class.java)

        val page = channel.parsePage(pageNumber, responseBody!!)

        // Filter already processed pages
        val pageUrls = page.memesInfo.mapNotNull { it.pageUrl }
        val existingUrls = memeService.findByPageUrls(pageUrls).map { it.pageUrl }.toSet()
        val newMemes = page.memesInfo.filter { !existingUrls.contains(it.pageUrl) }

        // Request memes indexation
        for (memeInfo in newMemes) {
            eventPublisher.publishEvent(MemeIndexRequest(channel, memeInfo))
        }

        // --
        eventPublisher.publishEvent(PageIndexedEvent(channel, page))
    }


    companion object {
        private val log = LoggerFactory.getLogger(PageIndexer::class.java)
    }

}

package me.ruslanys.ifunny.grab

import me.ruslanys.ifunny.grab.event.MemeIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.service.MemeService
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
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
            .requestFactory {
                HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().disableRedirectHandling().build())
            }
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

        val memesInfo = channel.parsePage(responseBody!!)
        log.trace("At the page [#{}|{}] #{} memes fetched", pageNumber, channel.getName(), memesInfo.size)

        // Filter already processed pages
        val existingUrls = memeService.findByPageUrls(memesInfo.map { it.pageUrl!! }).map { it.pageUrl }.toSet()
        val newMemes = memesInfo.filter { !existingUrls.contains(it.pageUrl) }

        // Request memes indexation
        for (memeInfo in newMemes) {
            eventPublisher.publishEvent(MemeIndexRequest(channel, memeInfo))
        }

        // --
        eventPublisher.publishEvent(PageIndexedEvent(channel, pageNumber, memesInfo))
    }


    companion object {
        private val log = LoggerFactory.getLogger(PageIndexer::class.java)
    }

}

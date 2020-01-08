package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import me.ruslanys.ifunny.base.ServiceTests
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.FunpotChannel
import me.ruslanys.ifunny.channel.Page
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexedEvent
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.repository.PageRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.context.ApplicationEventPublisher


class CoordinatorTests : ServiceTests() {

    // @formatter:off
    @Mock private lateinit var eventPublisher: ApplicationEventPublisher
    @Mock private lateinit var pageRepository: PageRepository
    // @formatter:on

    private val channels = listOf(DebesteChannel(), FunpotChannel())
    private val grabProperties = GrabProperties()

    private lateinit var coordinator: Coordinator

    @BeforeEach
    fun setUp() {
        coordinator = Coordinator(channels, eventPublisher, pageRepository, grabProperties)
    }

    @Test
    fun initializationShouldStartsWithTheFirstPagePerEachChannel() {
        given(pageRepository.incCurrent(any())).willReturn(1)

        // --
        coordinator.initializeGrabbing()

        // --
        verify(eventPublisher, times(channels.size)).publishEvent(any<PageIndexRequest>())
    }

    @Test
    fun `When a channel is not indexed and there is a next page`() {
        given(pageRepository.getLast(any())).willReturn(null) // not indexed
        given(pageRepository.incCurrent(any())).willReturn(5)

        // --
        coordinator.onIndexedPage(PageIndexedEvent(channels.first(), Page(1, true, listOf()), 2))

        // --
        verify(eventPublisher).publishEvent(any<PageIndexRequest>())
    }

    @Test
    fun `When a channel is not indexed and there is no next page`() {
        given(pageRepository.getLast(any())).willReturn(null) // not indexed

        // --
        coordinator.onIndexedPage(PageIndexedEvent(channels.first(), Page(100, false, listOf()), 2))

        // --
        verify(pageRepository).setLast(channels.first(), 100, grabProperties.retention.fullIndex)
        verify(pageRepository).clearCurrent(channels.first())
        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `When a channel is fully indexed it should index pages until it has new memes`() {
        given(pageRepository.getLast(any())).willReturn(100) // indexed
        given(pageRepository.incCurrent(any())).willReturn(5)

        // --
        coordinator.onIndexedPage(PageIndexedEvent(channels.first(), Page(100, true, listOf()), 1))

        // --
        verify(eventPublisher).publishEvent(any<PageIndexRequest>())
    }

    @Test
    fun `When a channel is fully indexed it should clear current page number when it has no new memes`() {
        given(pageRepository.getLast(any())).willReturn(100) // indexed

        // --
        coordinator.onIndexedPage(PageIndexedEvent(channels.first(), Page(100, true, listOf()), 0))

        // --
        verify(pageRepository).clearCurrent(channels.first())
    }

}

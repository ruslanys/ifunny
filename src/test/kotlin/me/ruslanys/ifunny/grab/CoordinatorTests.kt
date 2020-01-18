package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.base.ServiceTests
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.FunpotChannel
import me.ruslanys.ifunny.channel.Page
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.grab.event.PageIndexRequest
import me.ruslanys.ifunny.grab.event.PageIndexSuccessful
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.repository.PageRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import reactor.core.publisher.Mono


class CoordinatorTests : ServiceTests() {

    // @formatter:off
    @Mock private lateinit var eventChannel: SendChannel<GrabEvent>
    @Mock private lateinit var pageRepository: PageRepository
    // @formatter:on

    private val channels = listOf(DebesteChannel(), FunpotChannel())
    private val grabProperties = GrabProperties()

    private lateinit var coordinator: Coordinator

    @BeforeEach
    fun setUp() {
        coordinator = Coordinator(channels, eventChannel, pageRepository, grabProperties)
    }

    @Test
    fun scheduleGrabbingShouldStartWithTheFirstPagePerEachChannel() = runBlocking {
        given(pageRepository.incCurrent(any())).willReturn(Mono.just(1))

        coordinator.scheduleGrabbing().join()
        verify(eventChannel, times(channels.size)).send(any<PageIndexRequest>())
    }

    @Test
    fun whileChannelIsNotIndexedItShouldContinueParsing() = runBlocking {
        given(pageRepository.getLast(any())).willReturn(Mono.empty()) // not indexed
        given(pageRepository.incCurrent(any())).willReturn(Mono.just(5))

        coordinator.handleEvent(PageIndexSuccessful(channels.first(), Page(1, true, listOf()), 2))
        verify(eventChannel).send(any<PageIndexRequest>())

    }

    @Test
    fun whenChannelIsNotIndexedAndItReachesTheLastPageItShouldFinalizeTheState() = runBlocking {
        given(pageRepository.getLast(any())).willReturn(Mono.empty()) // not indexed
        given(pageRepository.setLast(any(), any(), any())).willReturn(Mono.just(true)) // Reactive stub
        given(pageRepository.clearCurrent(any())).willReturn(Mono.just(1)) // Reactive stub

        // --
        coordinator.handleEvent(PageIndexSuccessful(channels.first(), Page(100, false, listOf()), 2))

        // --
        verify(pageRepository).setLast(channels.first(), 100, grabProperties.retention.fullIndex)
        verify(pageRepository).clearCurrent(channels.first())
        verify(eventChannel, never()).send(any())
    }

    @Test
    fun fullyIndexedChannelShouldBeParsedUntilItHasNewMemes() = runBlocking {
        given(pageRepository.getLast(any())).willReturn(Mono.just(100)) // indexed
        given(pageRepository.incCurrent(any())).willReturn(Mono.just(5))

        // --
        val hasNext = true
        val new = 1

        coordinator.handleEvent(PageIndexSuccessful(channels.first(), Page(100, hasNext, listOf()), new))

        // --
        verify(eventChannel).send(any<PageIndexRequest>())
    }

    @Test
    fun fullyIndexedChannelShouldCleanThePositionWhenItReachesTheOldPage() = runBlocking {
        given(pageRepository.getLast(any())).willReturn(Mono.just(100)) // indexed
        given(pageRepository.clearCurrent(any())).willReturn(Mono.just(1)) // Reactive stub

        // --
        val hasNext = true
        val new = 0

        coordinator.handleEvent(PageIndexSuccessful(channels.first(), Page(100, hasNext, listOf()), new))

        // --
        verify(pageRepository).clearCurrent(channels.first())
        verify(eventChannel, never()).send(any<PageIndexRequest>())
    }

    @Test
    fun fullyIndexedChannelShouldCleanThePositionWhenItReachesTheLast() = runBlocking {
        given(pageRepository.getLast(any())).willReturn(Mono.just(100)) // indexed
        given(pageRepository.clearCurrent(any())).willReturn(Mono.just(1)) // Reactive stub

        // --
        val hasNext = false
        val new = 1

        coordinator.handleEvent(PageIndexSuccessful(channels.first(), Page(100, hasNext, listOf()), new))

        // --
        verify(pageRepository).clearCurrent(channels.first())
        verify(eventChannel, never()).send(any<PageIndexRequest>())
    }

}

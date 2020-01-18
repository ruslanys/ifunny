package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.base.RedisRepositoryTests
import me.ruslanys.ifunny.channel.DebesteChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class PageRepositoryTests : RedisRepositoryTests() {

    private lateinit var repository: PageRepository

    @BeforeEach
    fun setUp() {
        repository = PageRepository(redisTemplate)
        redisTemplate.execute {
            it.serverCommands().flushAll()
        }.blockFirst()
    }

    @Test
    fun getCurrentShouldReturnFirst() {
        val channel = DebesteChannel()
        val currentPageNumber = repository.getCurrent(channel).block()
        assertThat(currentPageNumber).isEqualTo(1)
    }

    @Test
    fun getCurrentShouldReturnPersistedValue() {
        val channel = DebesteChannel()
        redisTemplate.opsForValue().set("$channel:current", 777).block()

        val pageNumber = repository.getCurrent(channel).block()

        assertThat(pageNumber).isEqualTo(777)
    }

    @Test
    fun incCurrentShouldReturnFirstByDefault() {
        val channel = DebesteChannel()

        val pageNumber = repository.incCurrent(channel).block()
        assertThat(pageNumber).isEqualTo(1)
    }

    @Test
    fun incCurrentShouldReturnSequence() {
        val channel = DebesteChannel()

        assertThat(repository.incCurrent(channel).block()).isEqualTo(1)
        assertThat(repository.incCurrent(channel).block()).isEqualTo(2)
        assertThat(repository.incCurrent(channel).block()).isEqualTo(3)
    }

    @Test
    fun incCurrentShouldReturnNext() {
        val channel = DebesteChannel()
        redisTemplate.opsForValue().set("$channel:current", 123).block()

        val pageNumber = repository.incCurrent(channel).block()

        assertThat(pageNumber).isEqualTo(124)
    }

    @Test
    fun getLastShouldReturnNullIfAbsent() {
        val last = repository.getLast(DebesteChannel()).block()
        assertThat(last).isNull()
    }

    @Test
    fun lastPageShouldExpire() {
        val channel = DebesteChannel()
        val duration = Duration.ofSeconds(2)

        repository.setLast(channel, 123, duration).block()

        // --
        val last = repository.getLast(channel).block()
        assertThat(last).isEqualTo(123)

        // --
        Thread.sleep(3_000)
        val lastExpired = repository.getLast(channel).block()
        assertThat(lastExpired).isNull()
    }

    @Test
    fun clearCurrentShouldRemoveKey() {
        val channel = DebesteChannel()
        val key = "$channel:current"

        redisTemplate.opsForValue().set(key, 123).block()
        assertThat(repository.getCurrent(channel).block()).isEqualTo(123)

        repository.clearCurrent(channel).block()
        assertThat(redisTemplate.opsForValue().get(key).block()).isNull()
    }


}

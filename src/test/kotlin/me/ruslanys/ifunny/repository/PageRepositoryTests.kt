package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.base.RedisRepositoryTests
import me.ruslanys.ifunny.channel.DebesteChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class PageRepositoryTests : RedisRepositoryTests() {

    private lateinit var repository: PageRepository

    @BeforeEach
    fun setUp() {
        repository = PageRepository(redisTemplate)
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.execute {
            it.flushAll()
        }
    }

    @Test
    fun getCurrentShouldReturnFirst() {
        val channel = DebesteChannel()
        val currentPageNumber = repository.getCurrent(channel)
        assertThat(currentPageNumber).isEqualTo(1)
    }

    @Test
    fun getCurrentShouldReturnPersistedValue() {
        val channel = DebesteChannel()
        redisTemplate.opsForValue()["$channel:current"] = 777L

        val pageNumber = repository.getCurrent(channel)

        assertThat(pageNumber).isEqualTo(777L)
    }

    @Test
    fun incCurrentShouldReturnFirstByDefault() {
        val channel = DebesteChannel()

        val pageNumber = repository.incCurrent(channel)
        assertThat(pageNumber).isEqualTo(1)
    }

    @Test
    fun incCurrentShouldReturnSequence() {
        val channel = DebesteChannel()

        assertThat(repository.incCurrent(channel)).isEqualTo(1)
        assertThat(repository.incCurrent(channel)).isEqualTo(2)
        assertThat(repository.incCurrent(channel)).isEqualTo(3)
    }

    @Test
    fun incCurrentShouldReturnNext() {
        val channel = DebesteChannel()
        redisTemplate.opsForValue()["$channel:${PageRepository.CURRENT_POSTFIX}"] = 123

        val pageNumber = repository.incCurrent(channel)

        assertThat(pageNumber).isEqualTo(124)
    }

    @Test
    fun getLastShouldReturnNullIfAbsent() {
        val last = repository.getLast(DebesteChannel())
        assertThat(last).isNull()
    }

    @Test
    fun lastPageShouldExpire()  {
        val channel = DebesteChannel()
        val duration = Duration.ofSeconds(2)

        repository.setLast(channel, 123, duration)

        // --
        val last = repository.getLast(channel)
        assertThat(last).isEqualTo(123)

        // --
        Thread.sleep(3_000)
        val lastExpired = repository.getLast(channel)
        assertThat(lastExpired).isNull()
    }

    @Test
    fun clearCurrentShouldRemoveKey() {
        val channel = DebesteChannel()
        val key = "$channel:current"

        redisTemplate.opsForValue()[key] = 123
        assertThat(repository.getCurrent(channel)).isEqualTo(123)

        repository.clearCurrent(channel)
        assertThat(redisTemplate.opsForValue()[key]).isNull()
    }


}

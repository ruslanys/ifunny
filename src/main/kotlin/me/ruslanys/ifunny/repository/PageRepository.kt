package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.channel.Channel
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class PageRepository(private val redisTemplate: ReactiveRedisTemplate<String, Any>) {

    private val valueOperations: ReactiveValueOperations<String, Any> = redisTemplate.opsForValue()

    fun getCurrent(channel: Channel): Mono<Int> {
        val key = currentKey(channel)
        return valueOperations[key].defaultIfEmpty(1).map { it as Int }
    }

    fun incCurrent(channel: Channel): Mono<Int> {
        val key = currentKey(channel)
        return valueOperations.increment(key).map { it.toInt() }
    }

    fun getLast(channel: Channel): Mono<Int> {
        val key = lastKey(channel)
        return valueOperations[key].map { it as Int }
    }

    fun setLast(channel: Channel, pageNumber: Int, retention: Duration): Mono<Boolean> {
        val key = lastKey(channel)
        return valueOperations.setIfAbsent(key, pageNumber, retention)
    }

    fun clearCurrent(channel: Channel): Mono<Long> {
        val key = currentKey(channel)
        return redisTemplate.delete(key)
    }

    private fun currentKey(channel: Channel): String {
        return "$channel:$CURRENT_POSTFIX"
    }

    private fun lastKey(channel: Channel): String {
        return "$channel:$LAST_POSTFIX"
    }


    companion object {
        private const val CURRENT_POSTFIX = "current"
        private const val LAST_POSTFIX = "last"
    }

}

package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.channel.Channel
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.concurrent.TimeUnit

@Repository
class PageRepository(private val redisTemplate: RedisTemplate<String, Any>) {

    private val valueOperations: ValueOperations<String, Any> = redisTemplate.opsForValue()

    fun getCurrent(channel: Channel): Int {
        val key = currentKey(channel)
        return valueOperations[key] as Int? ?: 1
    }

    fun incCurrent(channel: Channel): Int {
        val key = currentKey(channel)
        return valueOperations.increment(key)!!.toInt()
    }

    fun getLast(channel: Channel): Int? {
        val key = lastKey(channel)
        return valueOperations[key] as Int?
    }

    fun setLast(channel: Channel, pageNumber: Int, retention: Duration) {
        val key = lastKey(channel)
        valueOperations.setIfAbsent(key, pageNumber, retention.toSeconds(), TimeUnit.SECONDS)
    }

    fun clearCurrent(channel: Channel) {
        val key = currentKey(channel)
        redisTemplate.delete(key)
    }

    private fun currentKey(channel: Channel): String {
        return "$channel:$CURRENT_POSTFIX"
    }

    private fun lastKey(channel: Channel): String {
        return "$channel:$LAST_POSTFIX"
    }



    companion object {
        const val CURRENT_POSTFIX = "current"
        const val LAST_POSTFIX = "last"
    }

}

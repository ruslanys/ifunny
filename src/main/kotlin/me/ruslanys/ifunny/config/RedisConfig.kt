package me.ruslanys.ifunny.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext


@Configuration
class RedisConfig {

    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory, objectMapper: ObjectMapper): ReactiveRedisTemplate<String, Any> {
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>(serializer).build()
        return ReactiveRedisTemplate(factory, serializationContext)
    }

}

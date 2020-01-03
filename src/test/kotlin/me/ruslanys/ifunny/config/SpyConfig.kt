package me.ruslanys.ifunny.config

import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.GenericApplicationContext

/**
 * Workaround to spy [org.springframework.context.ApplicationEventPublisher].
 */
@Configuration
class SpyConfig {

    @Bean
    @Primary
    fun genericApplicationContext(gac: GenericApplicationContext): GenericApplicationContext {
        return Mockito.spy(gac)
    }

}

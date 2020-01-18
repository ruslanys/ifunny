package me.ruslanys.ifunny.config

import kotlinx.coroutines.channels.Channel
import me.ruslanys.ifunny.grab.event.GrabEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventChannelConfig {

    @Bean
    fun eventChannel(): Channel<GrabEvent> {
        return Channel(512)
    }

}

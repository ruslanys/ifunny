package me.ruslanys.ifunny.config

import kotlinx.coroutines.channels.Channel
import me.ruslanys.ifunny.grab.event.GrabEvent
import me.ruslanys.ifunny.property.GrabProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventChannelConfig(private val properties: GrabProperties) {

    @Bean
    fun eventChannel(): Channel<GrabEvent> {
        return Channel(properties.channelSize)
    }

}

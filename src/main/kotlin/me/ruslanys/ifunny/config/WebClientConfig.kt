package me.ruslanys.ifunny.config

import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import me.ruslanys.ifunny.property.GrabProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.resources.LoopResources


/**
 * https://github.com/spring-projects/spring-framework/blob/master/src/docs/asciidoc/web/webflux-webclient.adoc
 */
@Configuration
class WebClientConfig(private val properties: GrabProperties) {

    @Bean
    fun resourceFactory() = ReactorResourceFactory().apply {
        isUseGlobalResources = false
        connectionProvider = ConnectionProvider.fixed("crawler", 100, 10_000)
        loopResources = LoopResources.create("crawler", 1, 4, true)
    }

    @Bean
    fun webClient(): WebClient = WebClient.builder()
            .codecs {
                it.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
            }
            .clientConnector(ReactorClientHttpConnector(resourceFactory()) { client ->
                client.tcpConfiguration { tcpClient ->
                    tcpClient.doOnConnected { connection ->
                        connection.addHandlerLast(ReadTimeoutHandler(10))
                        connection.addHandlerLast(WriteTimeoutHandler(10))
                    }
                }

                client.secure()
                client.keepAlive(false)
                client.followRedirect(true)
            })
            .defaultHeader("User-Agent", properties.userAgent)
            .build()

}

package me.ruslanys.ifunny.controller

import me.ruslanys.ifunny.base.ControllerTests
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@WebFluxTest(FeedController::class)
class FeedControllerTests : ControllerTests() {

    @Test
    fun indexHtmlShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/index.html")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun indexHtmShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/index.htm")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun indexShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/index")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun rootShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun feedShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/feed")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun feedHtmlShouldReturnFeedFrontend() {
        webClient.get()
                .uri("/feed.html")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

    @Test
    fun postRequestShouldNotBeAllowed() {
        webClient.post()
                .uri("/feed.html")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
    }

}

package me.ruslanys.ifunny.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.base.ControllerTests
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyMeme
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(MemeController::class, GlobalExceptionHandler::class)
class MemeControllerTests : ControllerTests() {

    @MockBean private lateinit var memeService: MemeService

    @Test
    fun showPageShouldReturn404StatusWhenMemeNotFound() = runBlocking<Unit> {
        given(memeService.getById(any())).willThrow(NotFoundException("Not found"))

        webClient.get()
                .uri("/meme/112233")
                .exchange()
                .expectStatus().isNotFound
                .expectBody<String>().isEqualTo("Not found")
    }

    @Test
    fun showPageShouldReturnModelAndView() = runBlocking<Unit> {
        val meme = createDummyMeme()
        given(memeService.getById("123")).willReturn(meme)

        webClient.get()
                .uri("/meme/123")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
    }

}

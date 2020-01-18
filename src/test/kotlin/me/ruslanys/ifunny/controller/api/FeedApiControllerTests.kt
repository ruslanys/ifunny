package me.ruslanys.ifunny.controller.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.base.ControllerTests
import me.ruslanys.ifunny.controller.GlobalExceptionHandler
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyFile
import me.ruslanys.ifunny.util.createDummyMeme
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@WebFluxTest(FeedApiController::class, GlobalExceptionHandler::class)
class FeedApiControllerTests : ControllerTests() {

    @MockBean
    private lateinit var memeService: MemeService


    @Test
    fun postMethodShouldNotBeAllowedToGetPage() {
        webClient.post()
                .uri("/api/feed")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
    }

    @Test
    fun getPageWithoutSpecifiedLanguageShouldReturn400() {
        webClient.get()
                .uri("/api/feed")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun getPageWithWrongLanguageCodeShouldReturn400() {
        webClient.get()
                .uri("/api/feed?language=ch")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun getPageWithWrongOffsetShouldReturn400() {
        webClient.get()
                .uri("/api/feed?language=de&offset=-1")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun getPageWithWrongLimitShouldReturn400() {
        webClient.get()
                .uri("/api/feed?language=de&limit=101")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun getPageWithRestrictedSortingShouldReturn400() {
        webClient.get()
                .uri("/api/feed?language=de&sortBy=author")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun getPageWithProperParametersShouldReturnPageResponse() = runBlocking<Unit> {
        val memes = listOf(
                createDummyMeme(title = "Title 1", file = createDummyFile(name = "1.jpg"), id = ObjectId("5e107cc18b12145d4624f23c")),
                createDummyMeme(title = "Title 2", file = createDummyFile(name = "2.jpg"), id = ObjectId("5e107cc18b12145d4624f23d")),
                createDummyMeme(title = "Title 3", file = createDummyFile(name = "3.jpg"), id = ObjectId("5e107cc18b12145d4624f23e"))
        )
        val pageRequest = FeedApiController.FeedPageRequest().apply {
            offset = 10
            setLimit(5)
            sortBy = "likes"
            sortDirection = Sort.Direction.DESC
        }

        given(memeService.getPage(Language.GERMAN, pageRequest)).willReturn(PageImpl<Meme>(memes))

        // --
        val json = FeedApiControllerTests::class.java.getResourceAsStream("feed_getPage.json").bufferedReader().use { it.readText() }

        // --
        webClient.get()
                .uri {
                    it.path("/api/feed")
                            .queryParam("language", "de")
                            .queryParam("offset", "10")
                            .queryParam("limit", "5")
                            .queryParam("sortBy", "likes")
                            .queryParam("sortDirection", "DESC")
                            .build()
                }

                .exchange()
                .expectStatus().isOk
                .expectBody().json(json)
    }

    @Test
    fun getByIdShouldReturn404() = runBlocking<Unit> {
        given(memeService.getById(any())).willThrow(NotFoundException("Not Found"))

        webClient.get()
                .uri("/api/feed/123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody().json("""
                    {
                        "status": 404,
                        "error": "Not Found"
                    }
                """.trimIndent())
    }

    @Test
    fun getByIdShouldReturnMemeDto() = runBlocking<Unit> {
        val meme = createDummyMeme(id = ObjectId("5e107cc18b12145d4624f23c"))
        given(memeService.getById("321")).willReturn(meme)

        val json = FeedApiControllerTests::class.java.getResourceAsStream("feed_getById.json").bufferedReader().use { it.readText() }

        webClient.get()
                .uri("/api/feed/321")
                .exchange()
                .expectStatus().isOk
                .expectBody().json(json)
    }

}

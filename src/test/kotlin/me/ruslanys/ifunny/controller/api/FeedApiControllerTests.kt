package me.ruslanys.ifunny.controller.api

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import me.ruslanys.ifunny.base.ControllerTests
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyFile
import me.ruslanys.ifunny.util.createDummyMeme
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FeedApiController::class)
class FeedApiControllerTests : ControllerTests() {

    @MockBean
    private lateinit var memeService: MemeService


    @Test
    fun postMethodShouldNotBeAllowedToGetPage() {
        mvc.perform(post("/api/feed"))
                .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun getPageWithoutSpecifiedLanguageShouldReturn400() {
        mvc.perform(get("/api/feed"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun getPageWithWrongLanguageCodeShouldReturn400() {
        mvc.perform(get("/api/feed?language=ch"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun getPageWithWrongOffsetShouldReturn400() {
        mvc.perform(get("/api/feed?language=de&offset=-1"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun getPageWithWrongLimitShouldReturn400() {
        mvc.perform(get("/api/feed?language=de&limit=101"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun getPageWithRestrictedSortingShouldReturn400() {
        mvc.perform(get("/api/feed?language=de&sortBy=author"))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun getPageWithProperParametersShouldReturnPageResponse() {
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
        val json = javaClass.getResourceAsStream("feed_getPage.json").bufferedReader().use { it.readText() }

        // --
        mvc.perform(get("/api/feed")
                .queryParam("language", "de")
                .queryParam("offset", "10")
                .queryParam("limit", "5")
                .queryParam("sortBy", "likes")
                .queryParam("sortDirection", "DESC"))

                .andExpect(status().isOk)
                .andExpect(content().json(json))
    }

    @Test
    fun getByIdShouldReturn404() {
        given(memeService.getById(any())).willThrow(NotFoundException::class.java)

        mvc.perform(get("/api/feed/123"))
                .andExpect(status().isNotFound)
    }

    @Test
    fun getByIdShouldReturnMemeDto() {
        val meme = createDummyMeme(id = ObjectId("5e107cc18b12145d4624f23c"))
        given(memeService.getById("321")).willReturn(meme)

        val json = javaClass.getResourceAsStream("feed_getById.json").bufferedReader().use { it.readText() }

        mvc.perform(get("/api/feed/321"))
                .andExpect(status().isOk)
                .andExpect(content().json(json))
    }

}

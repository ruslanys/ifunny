package me.ruslanys.ifunny.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import me.ruslanys.ifunny.base.ControllerTests
import me.ruslanys.ifunny.controller.dto.MemeDto
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.createDummyMeme
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(MemeController::class)
class MemeControllerTests : ControllerTests() {

    @MockBean private lateinit var memeService: MemeService

    @Test
    fun showPageShouldReturn404StatusWhenMemeNotFound() {
        given(memeService.getById(any())).willThrow(NotFoundException("Not found"))

        mvc.perform(get("/meme/${UUID.randomUUID()}"))
                .andExpect(status().isNotFound)
    }

    @Test
    fun showPageShouldReturnModelAndView() {
        val meme = createDummyMeme()
        given(memeService.getById("123")).willReturn(meme)

        mvc.perform(get("/meme/123"))
                .andExpect(status().isOk)
                .andExpect(view().name("meme"))
                .andExpect(model().attribute("meme", MemeDto(meme)))
    }

}

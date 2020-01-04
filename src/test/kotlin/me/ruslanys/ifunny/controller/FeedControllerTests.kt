package me.ruslanys.ifunny.controller

import me.ruslanys.ifunny.base.ControllerTests
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(FeedController::class)
class FeedControllerTests : ControllerTests() {

    @Test
    fun indexHtmlShouldReturnFeedFrontend() {
        mvc.perform(get("/index.html"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun indexHtmShouldReturnFeedFrontend() {
        mvc.perform(get("/index.htm"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun indexShouldReturnFeedFrontend() {
        mvc.perform(get("/index"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun emptyPathShouldReturnFeedFrontend() {
        mvc.perform(get("/"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun feedShouldReturnFeedFrontend() {
        mvc.perform(get("/feed"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun feedHtmlShouldReturnFeedFrontend() {
        mvc.perform(get("/feed.html"))

                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "text/html;charset=UTF-8"))
                .andExpect(view().name("feed"))
    }

    @Test
    fun postRequestShouldNotBeAllowed() {
        mvc.perform(post("/feed.html"))
                .andExpect(status().isMethodNotAllowed)
    }

}

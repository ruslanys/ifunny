package me.ruslanys.ifunny.controller

import me.ruslanys.ifunny.controller.dto.MemeDto
import me.ruslanys.ifunny.service.MemeService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

@Controller
class MemeController(private val memeService: MemeService) {

    @GetMapping("/meme/{id}")
    fun showMeme(@PathVariable id: String): ModelAndView {
        val meme = memeService.getById(id)
        return ModelAndView("meme", "meme", MemeDto(meme))
    }

}

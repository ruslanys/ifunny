package me.ruslanys.ifunny.controller

import me.ruslanys.ifunny.controller.dto.MemeDto
import me.ruslanys.ifunny.service.MemeService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class MemeController(private val memeService: MemeService) {

    @GetMapping("/meme/{id}")
    suspend fun showMeme(@PathVariable id: String, model: Model): String {
        val meme = memeService.getById(id)
        model.addAttribute("meme", MemeDto(meme))
        return "meme"
    }

}

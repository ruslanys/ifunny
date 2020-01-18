package me.ruslanys.ifunny.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SwaggerUiController {

    @GetMapping("/swagger-ui", "/swagger-ui.html")
    fun swaggerUi(): String {
        return "redirect:/swagger-ui/index.html"
    }

}

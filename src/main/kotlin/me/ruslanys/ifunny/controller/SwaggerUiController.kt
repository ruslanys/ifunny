package me.ruslanys.ifunny.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SwaggerUiController {

    @GetMapping("/swagger-ui", "/swagger-ui.html")
    fun swaggerUi(): String {
        return "redirect:/webjars/swagger-ui/3.24.3/index.html?url=/v3/api-docs"
    }

}

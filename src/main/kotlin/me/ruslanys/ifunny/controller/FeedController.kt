package me.ruslanys.ifunny.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class FeedController {

    @GetMapping("/", "/index", "/index.htm", "/index.html", "/feed", "/feed.html")
    fun showFeed(): String = "feed"

}

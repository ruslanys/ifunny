package me.ruslanys.ifunny.base

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient


abstract class ControllerTests {

    @Autowired
    protected lateinit var webClient: WebTestClient


}

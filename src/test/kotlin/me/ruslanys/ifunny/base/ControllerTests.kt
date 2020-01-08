package me.ruslanys.ifunny.base

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc

abstract class ControllerTests {

    @Autowired
    protected lateinit var mvc: MockMvc

}

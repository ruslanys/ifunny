package me.ruslanys.ifunny.base

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc

@ExtendWith(SpringExtension::class)
abstract class ControllerTests {

    @Autowired
    protected lateinit var mvc: MockMvc

}

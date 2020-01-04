package me.ruslanys.ifunny.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import me.ruslanys.ifunny.base.ServiceTests
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.repository.MemeRepository
import me.ruslanys.ifunny.util.createDummyMeme
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import java.util.*

class DefaultMemeServiceTests : ServiceTests() {

    @Mock private lateinit var memeRepository: MemeRepository

    private lateinit var service: DefaultMemeService

    @BeforeEach
    fun setUp() {
        service = DefaultMemeService(memeRepository)
    }

    @Test
    fun getByIdShouldThrowNotFoundException() {
        given(memeRepository.findById(any())).willReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            service.getById("123")
        }
    }

    @Test
    fun getByIdShouldReturnValueFromRepository() {
        val meme = createDummyMeme()
        given(memeRepository.findById("321")).willReturn(Optional.of(meme))

        val actual = service.getById("321")

        assertEquals(meme, actual)
    }

}

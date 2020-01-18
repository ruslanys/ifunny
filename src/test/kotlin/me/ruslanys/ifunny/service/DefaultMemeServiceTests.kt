package me.ruslanys.ifunny.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.given
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.base.ServiceTests
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.controller.dto.PageRequest
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.exception.NotFoundException
import me.ruslanys.ifunny.repository.MemeRepository
import me.ruslanys.ifunny.util.createDummyFile
import me.ruslanys.ifunny.util.createDummyMeme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DefaultMemeServiceTests : ServiceTests() {

    @Mock private lateinit var memeRepository: MemeRepository

    private lateinit var service: DefaultMemeService

    @BeforeEach
    fun setUp() {
        service = DefaultMemeService(memeRepository)
    }

    @Test
    fun addShouldReturnValueAfterSave() = runBlocking<Unit> {
        val meme = createDummyMeme()
        val info = MemeInfo(pageUrl = "", title = "", originUrl = "")

        given(memeRepository.save(any<Meme>())).willReturn(Mono.just(meme))

        val result = service.add(DebesteChannel(), info, createDummyFile(), "fingerprint")

        assertThat(result).isEqualTo(meme)
    }

    @Test
    fun addShouldThrowExceptionWhenPageUrlIsNull() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                service.add(DebesteChannel(), MemeInfo(title = "", originUrl = "", pageUrl = null), createDummyFile(), null)
            }
        }
    }

    @Test
    fun addShouldThrowExceptionWhenTitleIsNull() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                service.add(DebesteChannel(), MemeInfo(title = null, originUrl = "", pageUrl = ""), createDummyFile(), null)
            }
        }
    }

    @Test
    fun addShouldThrowExceptionWhenOriginUrlIsNull() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                service.add(DebesteChannel(), MemeInfo(title = "", originUrl = null, pageUrl = ""), createDummyFile(), null)
            }
        }
    }

    @Test
    fun isExistsShouldReturnFalseWhenThereIsNoSuchDocument() = runBlocking<Unit> {
        given(memeRepository.existsByLanguageAndFingerprint("de", "fingerprint")).willReturn(Mono.just(false))

        val result = service.isExists(Language.GERMAN, "fingerprint")

        assertThat(result).isFalse()
    }

    @Test
    fun isExistsShouldReturnTrueWhenThereIsSuchDocument() = runBlocking<Unit> {
        given(memeRepository.existsByLanguageAndFingerprint("de", "fingerprint")).willReturn(Mono.just(true))

        val result = service.isExists(Language.GERMAN, "fingerprint")

        assertThat(result).isTrue()
    }

    @Test
    fun findByPageUrlsShouldReturnList() = runBlocking<Unit> {
        val memes = arrayOf(createDummyMeme(), createDummyMeme(), createDummyMeme())
        given(memeRepository.findByPageUrlIn(any())).willReturn(Flux.just(*memes))

        val result = service.findByPageUrls(listOf())

        assertThat(result).contains(*memes)
    }

    @Test
    fun getPageShouldReturnPageableResponse() = runBlocking<Unit> {
        val memes = arrayOf(createDummyMeme(), createDummyMeme())
        given(memeRepository.countByLanguage(Language.GERMAN.code)).willReturn(Mono.just(101))
        given(memeRepository.findByLanguage(eq(Language.GERMAN.code), any<PageRequest>())).willReturn(Flux.just(*memes))

        val page = service.getPage(Language.GERMAN, PageRequest())

        assertThat(page.totalElements).isEqualTo(101)
        assertThat(page.content).contains(*memes)
    }

    @Test
    fun getByIdShouldThrowNotFoundException() {
        given(memeRepository.findById(any<String>())).willReturn(Mono.empty())

        assertThrows(NotFoundException::class.java) {
            runBlocking {
                service.getById("123")
            }
        }
    }

    @Test
    fun getByIdShouldReturnValueFromRepository() = runBlocking {
        val meme = createDummyMeme()
        given(memeRepository.findById("321")).willReturn(Mono.just(meme))

        val actual = service.getById("321")

        assertEquals(meme, actual)
    }

}

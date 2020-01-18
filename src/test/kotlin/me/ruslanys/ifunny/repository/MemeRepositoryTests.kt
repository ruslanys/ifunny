package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.base.MongoRepositoryTests
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.util.createDummyMeme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

class MemeRepositoryTests : MongoRepositoryTests() {

    @Autowired
    private lateinit var repository: MemeRepository

    @AfterEach
    fun tearDown() {
        repository.deleteAll().block()
    }

    @Test
    fun persistTest() {
        val meme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 12, 12, 12, 12))).block()!!

        // --
        val memeFromDb = mongoTemplate.findById(meme.id, Meme::class.java)

        // --
        assertThat(memeFromDb).isNotNull
        assertThat(memeFromDb).isEqualTo(meme)
    }

    @Test
    fun breakUniqueConstraintTest() {
        val firstMeme = createDummyMeme(pageUrl = UUID.randomUUID().toString())
        val secondMeme = createDummyMeme(pageUrl = firstMeme.pageUrl)

        repository.save(firstMeme).block()

        assertThrows<DuplicateKeyException> {
            repository.save(secondMeme).block()
        }
    }

    @Test
    fun findByLanguageTest() {
        for (i in 1..30) {
            repository.save(createDummyMeme(language = Language.PORTUGUESE)).block()
        }
        repository.save(createDummyMeme(language = Language.RUSSIAN)).block()

        // --
        val list = repository.findByLanguage(Language.PORTUGUESE.code, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .collectList()
                .block()!!

        val count = repository.countByLanguage(Language.PORTUGUESE.code).block()!!

        // --
        assertThat(count).isEqualTo(30)
        assertThat(list.size).isEqualTo(10)
    }

    @DisplayName("""
        The page should display memes with publish date sorted by date DESC, and after that, memes without publishing date sorted by ID DESC.
    """)
    @Test
    fun sortByDateTest() {
        val firstMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 1, 10, 0))).block()
        val secondMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 2, 10, 0))).block()
        val thirdMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 3, 10, 0))).block()
        val emptyDateFirst = repository.save(createDummyMeme(publishDateTime = null)).block()
        val emptyDateSecond = repository.save(createDummyMeme(publishDateTime = null)).block()
        val emptyDateThird = repository.save(createDummyMeme(publishDateTime = null)).block()

        // --
        val count = repository.countByLanguage(Language.GERMAN.code).block()!!
        val list = repository.findByLanguage(Language.GERMAN.code, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishDateTime", "id")))
                .collectList()
                .block()!!

        // --
        assertThat(count).isEqualTo(6)
        assertThat(list).containsExactly(thirdMeme, secondMeme, firstMeme, emptyDateThird, emptyDateSecond, emptyDateFirst)
    }

    @Test
    fun shouldExistsByLanguageAndFingerprint() {
        val fingerprint = "FINGERPRINT"
        val language = Language.GERMAN

        mongoTemplate.save(createDummyMeme(language = language, fingerprint = fingerprint))

        // --
        val result = repository.existsByLanguageAndFingerprint(language.code, fingerprint).block()

        // --
        assertThat(result).isTrue()
    }

    @Test
    fun shouldNotExistsByLanguageAndFingerprintWhereLanguageIsDifferent() {
        val fingerprint = "FINGERPRINT"

        mongoTemplate.save(createDummyMeme(language = Language.GERMAN, fingerprint = fingerprint))

        // --
        val result = repository.existsByLanguageAndFingerprint(Language.RUSSIAN.code, fingerprint).block()

        // --
        assertThat(result).isFalse()
    }

    @Test
    fun shouldNotExistsByLanguageAndFingerprintWhereFingerprintIsDifferent() {
        val language = Language.GERMAN

        mongoTemplate.save(createDummyMeme(language = language, fingerprint = "123"))

        // --
        val result = repository.existsByLanguageAndFingerprint(language.code, "321").block()

        // --
        assertThat(result).isFalse()
    }

    @Test
    fun findByPageUrls() {
        val urls = arrayListOf("http://debeste.de/meme-1", "http://debeste.de/meme-2", "http://debeste.de/meme-3")
        urls.forEach {
            mongoTemplate.save(createDummyMeme(pageUrl = it))
        }

        // --
        val result = repository.findByPageUrlIn(urls)
                .collectList()
                .block()!!

        // --
        assertThat(result).hasSize(urls.size)
        assertThat(result.map { it.pageUrl }).containsExactlyElementsOf(urls)
    }

}

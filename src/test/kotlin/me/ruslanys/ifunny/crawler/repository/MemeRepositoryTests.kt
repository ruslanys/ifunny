package me.ruslanys.ifunny.crawler.repository

import me.ruslanys.ifunny.crawler.base.RepositoryTests
import me.ruslanys.ifunny.crawler.domain.Language
import me.ruslanys.ifunny.crawler.domain.Meme
import me.ruslanys.ifunny.crawler.util.createDummyMeme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

class MemeRepositoryTests : RepositoryTests() {

    @Autowired
    private lateinit var repository: MemeRepository

    @BeforeEach
    override fun setUp() {
        super.setUp()
        repository.deleteAll()
    }

    @Test
    fun persistTest() {
        val meme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 12, 12, 12, 12)))

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

        repository.save(firstMeme)

        assertThrows<DuplicateKeyException> {
            repository.save(secondMeme)
        }
    }

    @Test
    fun findByLanguageTest() {
        for (i in 1..30) {
            repository.save(createDummyMeme(language = Language.PORTUGUESE))
        }
        repository.save(createDummyMeme(language = Language.RUSSIAN))

        // --
        val page = repository.findByLanguage(Language.PORTUGUESE.code, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))

        // --
        assertThat(page.totalElements).isEqualTo(30)
        assertThat(page.content.size).isEqualTo(10)
    }

    @DisplayName("""
        The page should display memes with publish date sorted by date DESC, and after that, memes without publishing date sorted by ID DESC.
    """)
    @Test
    fun sortByDateTest() {
        val firstMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 1, 10, 0)))
        val secondMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 2, 10, 0)))
        val thirdMeme = repository.save(createDummyMeme(publishDateTime = LocalDateTime.of(2019, 12, 3, 10, 0)))
        val emptyDateFirst = repository.save(createDummyMeme(publishDateTime = null))
        val emptyDateSecond = repository.save(createDummyMeme(publishDateTime = null))
        val emptyDateThird = repository.save(createDummyMeme(publishDateTime = null))

        // --
        val page = repository.findByLanguage(Language.GERMAN.code, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishDateTime", "id")))

        // --
        assertThat(page.totalElements).isEqualTo(6)
        assertThat(page.content).containsExactly(thirdMeme, secondMeme, firstMeme, emptyDateThird, emptyDateSecond, emptyDateFirst)
    }

}

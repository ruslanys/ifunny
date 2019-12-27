package me.ruslanys.ifunny.crawler.repository

import me.ruslanys.ifunny.crawler.base.RepositoryTests
import me.ruslanys.ifunny.crawler.domain.Meme
import me.ruslanys.ifunny.crawler.util.createDummyMeme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import java.time.LocalDateTime
import java.util.*

class MemeRepositoryTests : RepositoryTests() {

    @Autowired
    private lateinit var repository: MemeRepository


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

}

package me.ruslanys.ifunny.crawler.repository

import me.ruslanys.ifunny.crawler.domain.Meme
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MemeRepository : MongoRepository<Meme, String> {

    fun findByLanguage(language: String, pageable: Pageable): Page<Meme>

}

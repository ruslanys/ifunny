package me.ruslanys.ifunny.repository

import me.ruslanys.ifunny.domain.Meme
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MemeRepository : ReactiveMongoRepository<Meme, String> {

    fun findByLanguage(language: String, pageable: Pageable): Flux<Meme>

    fun findByPageUrlIn(pageUrls: List<String>): Flux<Meme>

    fun existsByLanguageAndFingerprint(language: String, fingerprint: String): Mono<Boolean>

    fun countByLanguage(language: String): Mono<Long>

}

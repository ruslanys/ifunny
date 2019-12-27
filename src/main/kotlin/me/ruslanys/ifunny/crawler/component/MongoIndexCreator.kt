package me.ruslanys.ifunny.crawler.component

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component

@Component
class MongoIndexCreator(private val mongoConverter: MongoConverter, private val mongoTemplate: MongoTemplate) {

    @EventListener(ApplicationReadyEvent::class)
    fun initIndicesAfterStartup() {
        val mappingContext = mongoConverter.mappingContext
        for (entity in mappingContext.persistentEntities) {
            val clazz = entity.type
            if (!clazz.isAnnotationPresent(Document::class.java)) {
                continue
            }

            val indexOps = mongoTemplate.indexOps(clazz)
            val resolver = MongoPersistentEntityIndexResolver(mappingContext)
            resolver.resolveIndexFor(clazz).forEach { indexOps.ensureIndex(it) }
        }
    }

}

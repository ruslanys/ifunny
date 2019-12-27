package me.ruslanys.ifunny.crawler.base

import me.ruslanys.ifunny.crawler.component.MongoIndexCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
abstract class RepositoryTests {

    // @formatter:off
    @Autowired protected lateinit var mongoTemplate: MongoTemplate
    @Autowired protected lateinit var mongoConverter: MongoConverter
    // @formatter:on


    @BeforeEach
    fun setUp() {
        val indexCreator = MongoIndexCreator(mongoConverter, mongoTemplate)
        indexCreator.initIndicesAfterStartup()
    }

}

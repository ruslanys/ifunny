package me.ruslanys.ifunny.base

import me.ruslanys.ifunny.repository.MongoIndexCreator
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter

@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
abstract class MongoRepositoryTests {

    // @formatter:off
    @Autowired protected lateinit var mongoTemplate: MongoTemplate
    @Autowired protected lateinit var mongoConverter: MongoConverter
    // @formatter:on


    @BeforeEach
    open fun setUp() {
        val indexCreator = MongoIndexCreator(mongoConverter, mongoTemplate)
        indexCreator.initIndicesAfterStartup()
    }

}

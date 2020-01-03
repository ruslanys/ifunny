package me.ruslanys.ifunny.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotNull

@ConstructorBinding
@ConfigurationProperties(prefix = "aws.s3")
@Validated
data class AwsS3Properties(
        @field:NotNull
        val accessKey: String? = null,

        @field:NotNull
        val secretKey: String? = null,

        @field:NotNull
        val region: String? = null,

        @field:NotNull
        val bucket: String? = null
)

package me.ruslanys.ifunny.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty

@ConstructorBinding
@ConfigurationProperties(prefix = "aws.s3")
@Validated
data class AwsS3Properties(
        @field:NotEmpty
        val accessKey: String? = null,

        @field:NotEmpty
        val secretKey: String? = null,

        @field:NotEmpty
        val region: String? = null,

        @field:NotEmpty
        val bucket: String? = null
)

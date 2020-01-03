package me.ruslanys.ifunny.config

import me.ruslanys.ifunny.property.AwsS3Properties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import javax.annotation.PostConstruct

@Configuration
class AwsS3Config(private val properties: AwsS3Properties) {

    @Bean
    fun s3Client(): S3Client = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey, properties.secretKey)))
            .region(Region.of(properties.region))
            .build()


    @PostConstruct
    fun createBucket() {
        try {
            s3Client().headBucket(HeadBucketRequest.builder().bucket(properties.bucket).build())
        } catch (e: NoSuchBucketException) {
            val response =  s3Client().createBucket(CreateBucketRequest.builder().acl(BucketCannedACL.PUBLIC_READ).bucket(properties.bucket).build())
            log.warn("A new S3 bucket created {}.", response.location())
        }
    }


    companion object {
        private val log = LoggerFactory.getLogger(AwsS3Config::class.java)
    }

}

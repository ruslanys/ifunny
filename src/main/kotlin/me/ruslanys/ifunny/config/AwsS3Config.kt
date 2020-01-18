package me.ruslanys.ifunny.config

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.property.AwsS3Properties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import java.time.Duration
import javax.annotation.PostConstruct

@Configuration
class AwsS3Config(private val properties: AwsS3Properties) {

    @Bean
    fun s3ClientBuilder(): S3AsyncClientBuilder = S3AsyncClient.builder()
            .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                    .writeTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(10))
                    .connectionMaxIdleTime(Duration.ofSeconds(10))
                    .connectionTimeout(Duration.ofSeconds(10))
                    .connectionAcquisitionTimeout(Duration.ofSeconds(10))
            )
            .serviceConfiguration {
                it.checksumValidationEnabled(false)
                it.chunkedEncodingEnabled(true)
            }
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey, properties.secretKey)))
            .region(Region.of(properties.region))


    @PostConstruct
    fun createBucket() = runBlocking<Unit> {
        val client = s3ClientBuilder().build()
        try {
            val request = HeadBucketRequest.builder().bucket(properties.bucket).build()
            client.headBucket(request).await()
        } catch (e: NoSuchBucketException) {
            val request = CreateBucketRequest.builder()
                    .acl(BucketCannedACL.PUBLIC_READ)
                    .bucket(properties.bucket)
                    .build()
            val response = client.createBucket(request).await()
            log.warn("A new S3 bucket created {}.", response.location())
        } finally {
            client.close()
        }
    }


    companion object {
        private val log = LoggerFactory.getLogger(AwsS3Config::class.java)
    }

}

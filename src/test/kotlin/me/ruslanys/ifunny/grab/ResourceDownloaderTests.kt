package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.AwsS3Properties
import me.ruslanys.ifunny.service.MemeService
import me.ruslanys.ifunny.util.mockGetBytes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.util.concurrent.CompletableFuture

class ResourceDownloaderTests {

    private val webClient: WebClient = mock()
    private val memeService: MemeService = mock()
    private val s3ClientBuilder: S3AsyncClientBuilder = mock()
    private val s3Client: S3AsyncClient = mock()
    private val s3Properties: AwsS3Properties = AwsS3Properties(region = "eu-central-1", bucket = "bucket")

    private val resourceDownloader = ResourceDownloader(webClient, memeService, s3ClientBuilder, s3Properties)

    @BeforeEach
    fun setUp() {
        given(s3ClientBuilder.build()).willReturn(s3Client)
    }

    @Test
    fun downloadResourceShouldPutDataToS3AndMongoDb() = runBlocking<Unit> {
        val originUrl = "http://debeste.de/meme.jpg"
        val resource = ClassPathResource("picture.jpg", ResourceDownloaderTests::class.java)
        val channel = DebesteChannel()
        val request = ResourceDownloadRequest(channel, MemeInfo(originUrl = originUrl))
        val future = CompletableFuture<PutObjectResponse>()
        future.complete(PutObjectResponse.builder().build())

        // --
        given(memeService.isExists(any(), anyString())).willReturn(false)
        given(s3Client.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).willReturn(future) // Reactive stub
        mockGetBytes(webClient, originUrl, resource)

        // --
        resourceDownloader.handleEvent(request)

        // --
        val channelCaptor = argumentCaptor<Channel>()
        val fileCaptor = argumentCaptor<S3File>()
        val fingerprintCaptor = argumentCaptor<String>()

        verify(memeService, times(1)).add(channelCaptor.capture(), any(), fileCaptor.capture(), fingerprintCaptor.capture())
        verify(s3Client, times(1)).putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())

        // --
        assertThat(channelCaptor.firstValue).isEqualTo(channel)
        assertThat(fileCaptor.firstValue.checksum).isEqualTo("5A267D4B21F34C2900D7BEC44684A4B2")
        assertThat(fingerprintCaptor.firstValue).isEqualTo("F3F3C40425813979")
    }

    @Test
    fun downloadResourceShouldAvoidDuplicatesByFingerprint() = runBlocking<Unit> {
        val originUrl = "http://debeste.de/meme.jpg"
        val resource = ClassPathResource("picture.jpg", ResourceDownloaderTests::class.java)
        val channel = DebesteChannel()
        val request = ResourceDownloadRequest(channel, MemeInfo(originUrl = originUrl))

        // --
        given(memeService.isExists(any(), anyString())).willReturn(true)
        mockGetBytes(webClient, originUrl, resource)

        // --
        resourceDownloader.handleEvent(request)

        // --
        verify(s3Client, never()).putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())
        verify(memeService, never()).add(any(), any(), any(), any())
    }

    @Test
    fun downloadResourceShouldThrowIllegalStateExceptionWhenThereIsEmptyBody() = runBlocking<Unit> {
        val originUrl = "http://debeste.de/meme.jpg"
        val channel = DebesteChannel()
        val request = ResourceDownloadRequest(channel, MemeInfo(originUrl = originUrl))

        // --
        given(memeService.isExists(any(), anyString())).willReturn(true)
        mockGetBytes(webClient, originUrl, ByteArray(0))

        // --
        assertThrows<IllegalStateException> {
            runBlocking {
                resourceDownloader.handleEvent(request)
            }
        }

        // --
        verify(s3Client, never()).putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())
        verify(memeService, never()).add(any(), any(), any(), any())
    }

}

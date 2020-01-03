package me.ruslanys.ifunny.grab

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import me.ruslanys.ifunny.channel.Channel
import me.ruslanys.ifunny.channel.DebesteChannel
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.AwsS3Properties
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.service.MemeService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@ExtendWith(SpringExtension::class)
@RestClientTest(ResourceDownloader::class, GrabProperties::class)
class ResourceDownloaderTests {

    // @formatter:off
    @Autowired private lateinit var resourceDownloader: ResourceDownloader
    @Autowired private lateinit var server: MockRestServiceServer

    @MockBean private lateinit var s3Properties: AwsS3Properties
    @MockBean private lateinit var s3Client: S3Client
    @MockBean private lateinit var memeService: MemeService
    // @formatter:on


    @BeforeEach
    fun setUp() {
        given(s3Properties.region).willReturn("eu-central-1")
        given(s3Properties.bucket).willReturn("bucket")
    }

    @Test
    fun downloadResourceShouldPutDataToS3AndMongoDb() {
        val originUrl = "http://debeste.de/meme.jpg"
        val resource = ClassPathResource("picture.jpg", javaClass)
        val channel = DebesteChannel()
        val request = ResourceDownloadRequest(channel, MemeInfo(originUrl = originUrl))


        // --
        given(memeService.isExists(any(), anyString())).willReturn(false)

        server.expect(requestTo(originUrl)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resource, MediaType.IMAGE_JPEG))


        // --
        resourceDownloader.downloadResource(request)


        // --
        val channelCaptor = argumentCaptor<Channel>()
        val fileCaptor = argumentCaptor<S3File>()
        val fingerprintCaptor = argumentCaptor<String>()

        verify(memeService, times(1)).add(channelCaptor.capture(), any(), fileCaptor.capture(), fingerprintCaptor.capture())
        verify(s3Client, times(1)).putObject(any<PutObjectRequest>(), any<RequestBody>())

        // --
        assertThat(channelCaptor.firstValue).isEqualTo(channel)
        assertThat(fileCaptor.firstValue.checksum).isEqualTo("5A267D4B21F34C2900D7BEC44684A4B2")
        assertThat(fingerprintCaptor.firstValue).isEqualTo("F3F3C40425813979")
    }

    @Test
    fun downloadResourceShouldAvoidDuplicatesByFingerprint() {
        val originUrl = "http://debeste.de/meme.jpg"
        val resource = ClassPathResource("picture.jpg", javaClass)
        val channel = DebesteChannel()
        val request = ResourceDownloadRequest(channel, MemeInfo(originUrl = originUrl))


        // --
        given(memeService.isExists(any(), anyString())).willReturn(true)

        server.expect(requestTo(originUrl)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resource, MediaType.IMAGE_JPEG))


        // --
        resourceDownloader.downloadResource(request)


        // --
        verify(s3Client, never()).putObject(any<PutObjectRequest>(), any<RequestBody>())
        verify(memeService, never()).add(any(), any(), any(), any())
    }

}

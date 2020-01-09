package me.ruslanys.ifunny.grab

import com.github.kilianB.hashAlgorithms.AverageHash
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.AwsS3Properties
import me.ruslanys.ifunny.property.GrabProperties
import me.ruslanys.ifunny.service.MemeService
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.event.EventListener
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO

@Component
class ResourceDownloader(
        restTemplateBuilder: RestTemplateBuilder,
        grabProperties: GrabProperties,
        private val s3Client: S3Client,
        private val memeService: MemeService,
        private val s3Properties: AwsS3Properties
) {

    private val hasher = AverageHash(64)
    private val tika = Tika()

    private val restTemplate = restTemplateBuilder
            .defaultHeader("User-Agent", grabProperties.userAgent)
            .build()

    /**
     * Downloading meme resource file and upload it to S3.
     */
    @Async
    @EventListener
    fun downloadResource(request: ResourceDownloadRequest) {
        val channel = request.channel
        val memeInfo = request.info

        // Download Resource
        val response = restTemplate.exchange(memeInfo.originUrl!!, HttpMethod.GET, null, ByteArray::class.java)
        val responseBody = response.body!!

        // File
        val fileExtension = memeInfo.originUrl.substringAfterLast(".")
        val fileName = "${UUID.randomUUID()}.$fileExtension"
        val contentType = response.headers.getFirst("Content-Type") ?: tika.detect(responseBody, fileName)
        val checksum = generateChecksum(responseBody)
        val fingerprint = if (contentType.startsWith("image")) {
            generateFingerprint(responseBody)
        } else {
            null
        }

        // Deduplication
        if (fingerprint != null && memeService.isExists(channel.language, fingerprint)) {
            log.info("Duplicate discovered. Fingerprint: [{}], URL: {}", fingerprint, memeInfo.pageUrl)
            return
        }

        // Upload to S3
        val file = S3File(s3Properties.region!!, s3Properties.bucket!!, fileName, contentType, checksum, responseBody.size.toLong())
        uploadFile(file, responseBody)
        log.trace("Meme {} downloaded to {}", memeInfo.originUrl, file.name)

        // Persist
        memeService.add(channel, memeInfo, file, fingerprint)
    }

    private fun generateChecksum(byteArray: ByteArray): String = DigestUtils.md5DigestAsHex(byteArray).toUpperCase()

    private fun generateFingerprint(byteArray: ByteArray): String {
        val image = ImageIO.read(ByteArrayInputStream(byteArray))
        return hasher.hash(image).hashValue.toString(16).toUpperCase()
    }

    private fun uploadFile(file: S3File, byteArray: ByteArray) {
        val request = PutObjectRequest.builder()
                .acl(ObjectCannedACL.PUBLIC_READ)
                .bucket(file.bucket)
                .key(file.name)
                .contentType(file.contentType)
                .contentLength(file.size)
                .build()

        s3Client.putObject(request, RequestBody.fromBytes(byteArray))
    }


    companion object {
        private val log = LoggerFactory.getLogger(ResourceDownloader::class.java)
    }

}

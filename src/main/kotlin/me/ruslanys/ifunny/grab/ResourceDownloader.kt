package me.ruslanys.ifunny.grab

import com.github.kilianB.hashAlgorithms.AverageHash
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.future.await
import me.ruslanys.ifunny.channel.MemeInfo
import me.ruslanys.ifunny.domain.S3File
import me.ruslanys.ifunny.grab.event.ResourceDownloadRequest
import me.ruslanys.ifunny.property.AwsS3Properties
import me.ruslanys.ifunny.service.MemeService
import org.apache.tika.Tika
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.bodyToFlow
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Component
class ResourceDownloader(
        private val webClient: WebClient,
        private val memeService: MemeService,
        private val s3ClientBuilder: S3AsyncClientBuilder,
        private val s3Properties: AwsS3Properties
) : SuspendedEventListener<ResourceDownloadRequest> {

    private val hasher = AverageHash(64)
    private val tika = Tika()

    /**
     * Downloading meme resource file and upload it to S3.
     */
    override suspend fun handleEvent(event: ResourceDownloadRequest) {
        val channel = event.channel
        val memeInfo = event.info

        // Download Resource
        val (contentTypeHeader, responseBody) = downloadResource(memeInfo)

        // File
        val fileName = generateFilename(memeInfo)
        val contentType = contentTypeHeader ?: tika.detect(responseBody, fileName)
        val checksum = generateChecksum(responseBody)
        val fingerprint = generateFingerprint(contentType, responseBody)

        // Deduplication
        if (fingerprint != null && memeService.isExists(channel.language, fingerprint)) {
            log.info("Duplicate discovered. Fingerprint: [{}], URL: {}", fingerprint, memeInfo.pageUrl)
            return
        }

        // Upload to S3
        val file = uploadFile(fileName, contentType, checksum, responseBody)

        // Persist
        memeService.add(channel, memeInfo, file, fingerprint)
    }

    private suspend fun downloadResource(memeInfo: MemeInfo): Pair<String?, ByteArray> {
        val response = webClient.get().uri(memeInfo.originUrl!!).awaitExchange()
        val contentTypeHeader = response.headers().asHttpHeaders().getFirst("Content-Type")

        ByteArrayOutputStream().use { container ->
            response.bodyToFlow<ByteArray>().collect { bytes ->
                container.writeBytes(bytes)
            }

            if (container.size() == 0) {
                throw IllegalStateException("Response body is empty.")
            }

            return contentTypeHeader to container.toByteArray()
        }
    }

    private fun generateFilename(memeInfo: MemeInfo): String {
        val fileExtension = memeInfo.originUrl!!
                .substringAfterLast(".")
                .substringBefore("?")
        return "${UUID.randomUUID()}.$fileExtension"
    }

    private fun generateChecksum(byteArray: ByteArray): String = DigestUtils.md5DigestAsHex(byteArray).toUpperCase()

    private fun generateFingerprint(contentType: String, byteArray: ByteArray): String? {
        return if (contentType.startsWith("image")) {
            val image = ImageIO.read(ByteArrayInputStream(byteArray))
            hasher.hash(image).hashValue.toString(16).toUpperCase()
        } else {
            null
        }
    }

    private suspend fun uploadFile(fileName: String, contentType: String, checksum: String, byteArray: ByteArray): S3File {
        val request = PutObjectRequest.builder()
                .acl(ObjectCannedACL.PUBLIC_READ)
                .bucket(s3Properties.bucket!!)
                .key(fileName)
                .contentType(contentType)
                .contentLength(byteArray.size.toLong())
                .build()

        val s3Client = s3ClientBuilder.build()
        s3Client.use {
            it.putObject(request, AsyncRequestBody.fromBytes(byteArray)).await()
        }

        return S3File(s3Properties.region!!, s3Properties.bucket!!, fileName, contentType, checksum, byteArray.size.toLong())
    }

    companion object {
        private val log = LoggerFactory.getLogger(ResourceDownloader::class.java)
    }

}

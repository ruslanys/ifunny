package phash

import com.github.kilianB.hash.Hash
import com.github.kilianB.hashAlgorithms.AverageHash
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@Disabled
class AverageHashTests {

    private val hasher = AverageHash(64)

    @Test
    fun differentWhiteTextOnBlackBackground() {
        val hashedResources = hashedResources("dataset-1")
        val hashes = hashedResources.map { it.hash.hashValue.toString(16) }
        assertThat(hashes.toSet()).hasSize(hashedResources.size)
    }

    @Test
    fun theSamePictureWithDifferentWhiteLabels() {
        val hashedResources = hashedResources("dataset-2")
        val hashes = hashedResources.map { it.hash.hashValue.toString(16) }
        assertThat(hashes.toSet()).hasSize(1)
    }

    @Test
    fun theSameTextWithDifferentWhiteLabels() {
        val hashedResources = hashedResources("dataset-3")
        val hashes = hashedResources.map { it.hash.hashValue.toString(16) }
        assertThat(hashes.toSet()).hasSize(1)
    }

    @Test
    fun theSameGifWithDifferentWhiteLabels() {
        val hashedResources = hashedResources("dataset-4")
        val hashes = hashedResources.map { it.hash.hashValue.toString(16) }
        assertThat(hashes.toSet()).hasSize(1)
    }

    @Test
    fun differentYellowTextOnGrayBackground() {
        val hashedResources = hashedResources("dataset-5")
        val hashes = hashedResources.map { it.hash.hashValue.toString(16) }
        assertThat(hashes.toSet()).hasSize(hashedResources.size)
    }

    private fun hashedResources(path: String): List<HashedResource> {
        val resources = RESOURCE_RESOLVER.getResources("$ROOT_DIRECTORY/$path/*")
        return resources.map {
            HashedResource(it, hasher.hash(it.file))
        }
    }

    data class HashedResource(val resource: Resource, val hash: Hash)

    companion object {
        private const val ROOT_DIRECTORY = "phash"
        private val RESOURCE_RESOLVER = PathMatchingResourcePatternResolver()
    }

}

package phash

import com.github.kilianB.hash.Hash
import com.github.kilianB.hashAlgorithms.AverageColorHash
import com.github.kilianB.hashAlgorithms.WaveletHash
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@Disabled
class CompoundHashTests {

    private val fastHasher = WaveletHash(32, 5)
    private val accurateHasher = AverageColorHash(128)

    private val similarityThreshold: Double = 0.2


    @Test
    fun differentWhiteTextOnBlackBackground() {
        assertNotEqualsHashes("dataset-1")
    }

    @Test
    fun theSamePictureWithDifferentWhiteLabels() {
        assertEqualsHashes("dataset-2")
    }

    @Test
    fun theSameTextWithDifferentWhiteLabels() {
        assertEqualsHashes("dataset-3")
    }

    @Test
    fun theSameGifWithDifferentWhiteLabels() {
        assertEqualsHashes("dataset-4")
    }

    @Test
    fun differentYellowTextOnGrayBackground() {
        assertNotEqualsHashes("dataset-5")
    }

    private fun assertEqualsHashes(path: String) {
        val hashedResources = hashedResources(path)
        val first = hashedResources.first()
        val distances = distances(hashedResources)

        assertThat(hashedResources).allMatch { it.fastHash == first.fastHash }
        assertThat(distances).allMatch { it < similarityThreshold }
    }

    private fun assertNotEqualsHashes(path: String) {
        val hashedResources = hashedResources(path)
        val first = hashedResources.first()
        val distances = distances(hashedResources)

        assertThat(hashedResources).anyMatch { it.fastHash != first.fastHash }
        assertThat(distances).allMatch { it > similarityThreshold }
    }

    private fun hashedResources(path: String): List<HashedResource> {
        val resources  = RESOURCE_RESOLVER.getResources("$ROOT_DIRECTORY/$path/*")
        return resources.map {
            HashedResource(it, fastHasher.hash(it.file), accurateHasher.hash(it.file))
        }
    }

    private fun distances(hashedResources: List<HashedResource>): List<Double> {
        val distances = arrayListOf<Double>()
        for (i in hashedResources.indices) {
            for (j in i + 1 until hashedResources.size) {
                val distance = hashedResources[i].accurateHash.normalizedHammingDistance(hashedResources[j].accurateHash)
                distances.add(distance)
            }
        }
        return distances
    }


    data class HashedResource(val resource: Resource, val fastHash: Hash, val accurateHash: Hash)

    companion object {
        private const val ROOT_DIRECTORY = "phash"
        private val RESOURCE_RESOLVER = PathMatchingResourcePatternResolver()
    }

}

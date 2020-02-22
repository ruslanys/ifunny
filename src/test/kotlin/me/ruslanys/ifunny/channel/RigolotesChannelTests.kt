package me.ruslanys.ifunny.channel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.mockGet
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class RigolotesChannelTests {

    // region Mocks
    private val webClient: WebClient = mock()

    private val redisTemplate: ReactiveRedisTemplate<String, Any> = mock()
    private val valueOperations: ReactiveValueOperations<String, Any> = mock()
    // endregion

    private lateinit var channel: RigolotesChannel


    @BeforeEach
    fun setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations)

        channel = RigolotesChannel(redisTemplate, webClient)
    }

    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        given(valueOperations["$channel:pages"]).willReturn(Mono.just(2000))

        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://rigolotes.fr/page/2000")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        given(valueOperations["$channel:pages"]).willReturn(Mono.just(2000))

        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://rigolotes.fr/page/1901")
    }

    @Test
    fun fetchPageTest() = runBlocking<Unit> {
        given(valueOperations["$channel:pages"]).willReturn(Mono.justOrEmpty(null))
        given(valueOperations.setIfAbsent(any(), any(), any())).willReturn(Mono.just(true))
        mockGet(webClient, channel.baseUrl, ClassPathResource("rigolotes/page.html", RigolotesChannelTests::class.java))

        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://rigolotes.fr/page/1786")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/page.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(7)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.likes != null }
        assertThat(list).allMatch { it.publishDateTime != null }
        assertThat(list).allMatch { it.author != null }
        assertThat(list).allMatch { it.comments == null }
    }

    @Test
    fun parsePageWithYoutubeShouldSkipVideo() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/page_with_youtube.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(8)
    }

    @Test
    fun parsePageWithVideo() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/page_with_video.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(list).hasSize(10)
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/page_last.html")

        // --
        val page = channel.parsePage(1, html)

        // --
        assertThat(page.hasNext).isFalse()
    }

    @Test
    fun parseInvalidPageShouldReturnEmptyList() = runBlocking {
        val page = channel.parsePage(1, "<html></html>")
        assertThat(page.hasNext).isFalse()
        assertThat(page.memesInfo).isEmpty()
    }

    @Test
    fun parsePageWithUpdatedUpvotes() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/page_broken_upvotes.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(list).hasSize(9) // minus negative
    }

    @Test
    fun parsePictureMeme() = runBlocking<Unit> {
        val baseInfo = MemeInfo(
                pageUrl = "https://rigolotes.fr/34696/hey-mec-t-es-juste-un-gros",
                title = "Hey mec, t'es juste un gros....",
                likes = 2,
                publishDateTime = LocalDateTime.of(2020, 9, 1, 7, 24),
                author = "margaux"
        )
        val html = readResource<RigolotesChannelTests>("rigolotes/meme_picture.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.publishDateTime).isEqualTo(baseInfo.publishDateTime)
        assertThat(info.author).isEqualTo(baseInfo.author)

        assertThat(info.originUrl).isEqualTo("https://rigolotes.fr/img/normal/20191210/012/20191210.jpg")
    }

    @Test
    fun parseGifMeme() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/meme_gif.html")

        // --
        val info = channel.parseMeme(MemeInfo(), html)

        // --
        assertThat(info.originUrl).isEqualTo("https://rigolotes.fr/img/normal/20140730/HZ3/142555.gif")
    }

    @Test
    fun parseVideoMeme() = runBlocking<Unit> {
        val html = readResource<RigolotesChannelTests>("rigolotes/meme_video.html")

        // --
        val info = channel.parseMeme(MemeInfo(), html)

        // --
        assertThat(info.originUrl).isEqualTo("https://rigolotes.fr/img/normal/20190910/ZIV/20190910.mp4")
    }

}


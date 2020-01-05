package me.ruslanys.ifunny.controller.api

import me.ruslanys.ifunny.controller.api.dto.PageRequest
import me.ruslanys.ifunny.controller.api.dto.PageResponse
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.domain.Meme
import me.ruslanys.ifunny.service.MemeService
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/api/feed")
class FeedApiController(private val memeService: MemeService) {

    @GetMapping
    fun getPage(@Valid searchRequest: FeedSearchRequest, @Valid pageRequest: FeedPageRequest): PageResponse<MemeDto> {
        val language = Language.findByCode(searchRequest.language!!)
        val page = memeService.getPage(language, pageRequest).map { MemeDto(it) }
        return PageResponse(page)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: String): MemeDto {
        val meme = memeService.getById(id)
        return MemeDto(meme)
    }


    data class FeedSearchRequest(
            @field:NotNull
            val language: String? = null
    )

    class FeedPageRequest : PageRequest(sortBy = "publishDateTime", sortDirection = Sort.Direction.DESC, maySortBy = setOf("publishDateTime", "likes"))

    data class MemeDto(
            val id: String,
            val language: String,
            val channelName: String,
            val title: String,
            val url: String,
            val contentType: String,
            val publishDateTime: LocalDateTime?,
            val author: String?,
            val likes: Int?,
            val comments: Int?
    ) {
        constructor(meme: Meme) : this(
                meme.id.toHexString(),
                meme.language,
                meme.channelName,
                meme.title,
                "https://${meme.file.bucket}.s3.${meme.file.region}.amazonaws.com/${meme.file.name}",
                meme.file.contentType,
                meme.publishDateTime,
                meme.author,
                meme.likes,
                meme.comments
        )
    }

}

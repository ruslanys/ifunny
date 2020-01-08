package me.ruslanys.ifunny.controller.api

import me.ruslanys.ifunny.controller.dto.MemeDto
import me.ruslanys.ifunny.controller.dto.PageRequest
import me.ruslanys.ifunny.controller.dto.PageResponse
import me.ruslanys.ifunny.domain.Language
import me.ruslanys.ifunny.service.MemeService
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    class FeedPageRequest : PageRequest(
            sortBy = "publishDateTime",
            sortDirection = Sort.Direction.DESC,
            maySortBy = setOf("publishDateTime", "likes")
    )

}

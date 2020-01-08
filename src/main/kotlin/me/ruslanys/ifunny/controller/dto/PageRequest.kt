package me.ruslanys.ifunny.controller.dto

import io.swagger.v3.oas.annotations.Hidden
import me.ruslanys.ifunny.controller.validation.SortConstraint
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@Suppress("unused")
@SortConstraint
open class PageRequest(
        @field:Min(value = 0) private var offset: Long = 0,
        @field:Min(value = 1) @field:Max(100) private var limit: Int = 10,
        var sortBy: String = "id",
        var sortDirection: Sort.Direction = Sort.Direction.ASC,
        private val maySortBy: Set<String> = setOf("id")
) : Pageable {

    @Hidden
    override fun getPageNumber(): Int = (offset / limit + 1).toInt()

    override fun next(): Pageable = PageRequest(offset + limit, limit, sortBy, sortDirection, maySortBy)

    @Hidden
    override fun getPageSize(): Int = limit

    fun setLimit(limit: Int) {
        this.limit = limit
    }

    override fun getOffset(): Long = offset

    fun setOffset(offset: Long) {
        this.offset = offset
    }

    override fun hasPrevious(): Boolean = offset > 0

    @Hidden
    override fun getSort(): Sort = Sort.by(sortDirection, sortBy)

    override fun first(): Pageable = PageRequest(0, limit, sortBy, sortDirection, maySortBy)

    private fun previous(): PageRequest {
        return if (offset == 0L) this else {
            var newOffset = this.offset - limit
            if (newOffset < 0) newOffset = 0
            PageRequest(newOffset, limit, sortBy, sortDirection, maySortBy)
        }
    }

    override fun previousOrFirst(): Pageable = if (hasPrevious()) previous() else first()

    @Hidden
    fun getMaySortBy(): Set<String> = maySortBy

    @Hidden
    override fun isPaged(): Boolean {
        return super.isPaged()
    }

    @Hidden
    override fun isUnpaged(): Boolean {
        return super.isUnpaged()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PageRequest

        if (offset != other.offset) return false
        if (limit != other.limit) return false
        if (sortBy != other.sortBy) return false
        if (sortDirection != other.sortDirection) return false
        if (maySortBy != other.maySortBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = offset
        result = 31 * result + limit
        result = 31 * result + sortBy.hashCode()
        result = 31 * result + sortDirection.hashCode()
        result = 31 * result + maySortBy.hashCode()
        return result.toInt()
    }

}

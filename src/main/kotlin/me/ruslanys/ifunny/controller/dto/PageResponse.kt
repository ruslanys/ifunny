package me.ruslanys.ifunny.controller.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(val totalCount: Long, val list: List<T>) {
    constructor(page: Page<T>) : this(page.totalElements, page.content)
    constructor(list: List<T>) : this(list.size.toLong(), list)
}

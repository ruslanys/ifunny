package me.ruslanys.ifunny.controller.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class PageRequestTests {

    @Test
    fun pageNumberShouldStartsWithOne() {
        val pageRequest = PageRequest(0, 10)
        assertThat(pageRequest.pageNumber).isEqualTo(1)
    }

    @Test
    fun nextPageTest() {
        val first = PageRequest(0, 10)

        val second = first.next()
        assertThat(second.offset).isEqualTo(10)
        assertThat(second.pageSize).isEqualTo(10)

        val third = second.next()
        assertThat(third.offset).isEqualTo(20)
        assertThat(third.pageSize).isEqualTo(10)
    }

    @Test
    fun firstPageShouldNotHavePrevious() {
        val pageRequest = PageRequest(0, 10)
        assertThat(pageRequest.hasPrevious()).isFalse()
    }

    @Test
    fun secondPageShouldHavePrevios() {
        val secondPage = PageRequest(10, 10)

        assertThat(secondPage.hasPrevious()).isTrue()
    }

    @Test
    fun previousOrFirstShouldReturnPrevious() {
        val third = PageRequest(20, 10)
        val second = PageRequest(10, 10)

        assertThat(third.previousOrFirst()).isEqualTo(second)
    }

    @Test
    fun previousOrFirstShouldReturnFirst() {
        val second = PageRequest(10, 10)
        val first = PageRequest(0, 10)

        assertThat(second.previousOrFirst()).isEqualTo(first)
    }

    @Test
    fun previousOrFirstForTheFirstPageShouldReturnFirst() {
        val first = PageRequest(0, 10)

        assertThat(first.previousOrFirst()).isEqualTo(first)
    }

    @Test
    fun hashCodeShouldBeEqualsIfObjectsAreEquals() {
        val first = PageRequest(0, 10)

        assertThat(first.hashCode()).isEqualTo(first.previousOrFirst().hashCode())
    }

    @Test
    fun hashCodeShouldBeDifferentIfObjectsAreDifferent() {
        val second = PageRequest(10, 10)

        assertThat(second.hashCode()).isNotEqualTo(second.previousOrFirst().hashCode())
    }

    @Test
    fun getSortAscTest() {
        val request = PageRequest(0, 10, sortBy = "fieldName", sortDirection = Sort.Direction.ASC)
        assertThat(request.sort).isEqualTo(Sort.by(Sort.Direction.ASC, "fieldName"))
    }

    @Test
    fun getSortDescTest() {
        val request = PageRequest(0, 10, sortBy = "name", sortDirection = Sort.Direction.DESC)
        assertThat(request.sort).isEqualTo(Sort.by(Sort.Direction.DESC, "name"))
    }

}

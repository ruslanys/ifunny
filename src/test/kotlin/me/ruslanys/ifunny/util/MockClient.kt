package me.ruslanys.ifunny.util

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Different options to mock the client: https://www.baeldung.com/spring-mocking-webclient
 */

fun mockGet(webClient: WebClient, path: String, response: String) {
    val get = mock<WebClient.RequestHeadersUriSpec<*>>()
    val uri = mock<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mock<WebClient.ResponseSpec>()

    given(webClient.get()).willReturn(get)
    given(get.uri(path)).willReturn(uri)
    given(uri.retrieve()).willReturn(responseSpec)
    given(responseSpec.bodyToMono(any<ParameterizedTypeReference<String>>())).willReturn(Mono.just(response))
}

fun mockGet(webClient: WebClient, path: String, resource: ClassPathResource) {
    val response = resource.inputStream.bufferedReader().use {
        it.readText()
    }
    mockGet(webClient, path, response)
}

fun mockGetBytes(webClient: WebClient, path: String, bytes: ByteArray) {
    val get = mock<WebClient.RequestHeadersUriSpec<*>>()
    val uri = mock<WebClient.RequestHeadersSpec<*>>()
    val response = mock<ClientResponse>()
    val headers = mock<ClientResponse.Headers>()
    val httpHeaders = HttpHeaders()

    given(webClient.get()).willReturn(get)
    given(get.uri(path)).willReturn(uri)
    given(uri.exchange()).willReturn(Mono.just(response))
    given(response.headers()).willReturn(headers)
    given(headers.asHttpHeaders()).willReturn(httpHeaders)
    given(response.bodyToFlux(any<ParameterizedTypeReference<*>>())).willReturn(Flux.just(bytes))
}

fun mockGetBytes(webClient: WebClient, path: String, resource: ClassPathResource) {
    val bytes = resource.inputStream.buffered().use {
        it.readAllBytes()
    }
    mockGetBytes(webClient, path, bytes)
}

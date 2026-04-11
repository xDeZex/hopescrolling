package com.hopescrolling.data.article

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RssFeedFetcherTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `successful 200 response returns body`() = runTest {
        server.enqueue(MockResponse().setBody("<rss/>").setResponseCode(200))
        val fetcher = httpRssFeedFetcher()
        val result = fetcher.fetch(server.url("/feed").toString())
        assertEquals("<rss/>", result)
    }

    @Test
    fun `404 response throws IOException with status code`() {
        server.enqueue(MockResponse().setResponseCode(404))
        val fetcher = httpRssFeedFetcher()
        val url = server.url("/feed").toString()
        val ex = assertThrows(IOException::class.java) { runBlocking { fetcher.fetch(url) } }
        assert(ex.message!!.contains("404")) { "Expected 404 in message: ${ex.message}" }
    }

    @Test
    fun `500 response throws IOException with status code`() {
        server.enqueue(MockResponse().setResponseCode(500))
        val fetcher = httpRssFeedFetcher()
        val url = server.url("/feed").toString()
        assertThrows(IOException::class.java) { runBlocking { fetcher.fetch(url) } }
    }

    @Test
    fun `malformed url throws IOException`() {
        val fetcher = httpRssFeedFetcher()
        assertThrows(IOException::class.java) { runBlocking { fetcher.fetch("not-a-url") } }
    }

    @Test
    fun `301 redirect is followed to new url`() = runTest {
        val finalUrl = server.url("/final").toString()
        server.enqueue(MockResponse().setResponseCode(301).addHeader("Location", finalUrl))
        server.enqueue(MockResponse().setBody("<rss/>").setResponseCode(200))
        val fetcher = httpRssFeedFetcher()
        val result = fetcher.fetch(server.url("/original").toString())
        assertEquals("<rss/>", result)
    }

    @Test
    fun `302 redirect is followed to new url`() = runTest {
        val finalUrl = server.url("/final").toString()
        server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", finalUrl))
        server.enqueue(MockResponse().setBody("<rss/>").setResponseCode(200))
        val fetcher = httpRssFeedFetcher()
        val result = fetcher.fetch(server.url("/original").toString())
        assertEquals("<rss/>", result)
    }

    @Test
    fun `too many redirects throws IOException`() {
        val url = server.url("/feed").toString()
        repeat(5) { server.enqueue(MockResponse().setResponseCode(301).addHeader("Location", url)) }
        val fetcher = httpRssFeedFetcher()
        val ex = assertThrows(IOException::class.java) { runBlocking { fetcher.fetch(url) } }
        assert(ex.message!!.contains("Too many redirects")) { "Expected 'Too many redirects' in: ${ex.message}" }
    }

    @Test
    fun `relative Location header is resolved against base url`() = runTest {
        server.enqueue(MockResponse().setResponseCode(301).addHeader("Location", "/final"))
        server.enqueue(MockResponse().setBody("<rss/>").setResponseCode(200))
        val fetcher = httpRssFeedFetcher()
        val result = fetcher.fetch(server.url("/original").toString())
        assertEquals("<rss/>", result)
    }

    @Test
    fun `201 response returns body`() = runTest {
        server.enqueue(MockResponse().setBody("<atom/>").setResponseCode(201))
        val fetcher = httpRssFeedFetcher()
        val result = fetcher.fetch(server.url("/feed").toString())
        assertEquals("<atom/>", result)
    }

    private fun <T : Throwable> assertThrows(clazz: Class<T>, block: () -> Unit): T {
        try {
            block()
            throw AssertionError("Expected ${clazz.simpleName} to be thrown")
        } catch (e: Throwable) {
            if (clazz.isInstance(e)) @Suppress("UNCHECKED_CAST") return e as T
            if (e is AssertionError) throw e
            throw e
        }
    }
}

package com.hopescrolling.data.article

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArticleContentFetcherTest {
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
    fun `returns failure on network error`() = runTest {
        val url = server.url("/").toString()
        server.shutdown()

        val result = jsoupArticleContentFetcher().fetch(url)

        assertTrue(result.isFailure)
    }

    @Test
    fun `returns failure on non-200 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        assertTrue(result.isFailure)
    }

    @Test
    fun `falls back to largest div by text length`() = runTest {
        val longText = "This is the longer content div with much more text than the other one here and then some more."
        val html = """
            <html><body>
                <div><p>Short</p></div>
                <div><p>$longText</p><p>And another paragraph here too.</p></div>
            </body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        val content = result.getOrThrow()
        assertTrue(content.paragraphs.contains(longText))
    }

    @Test
    fun `falls back to main element when no article or role=main`() = runTest {
        val html = """
            <html><head><title>Test</title></head>
            <body>
                <nav><p>Nav link</p></nav>
                <main><p>Main content</p></main>
            </body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        val content = result.getOrThrow()
        assertEquals(listOf("Main content"), content.paragraphs)
    }

    @Test
    fun `falls back to role=main when no article element`() = runTest {
        val html = """
            <html><head><title>Test</title></head>
            <body>
                <nav><p>Nav link</p></nav>
                <div role="main"><p>Main role content</p></div>
            </body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        val content = result.getOrThrow()
        assertEquals(listOf("Main role content"), content.paragraphs)
    }

    @Test
    fun `article element takes priority over role=main`() = runTest {
        val html = """
            <html><body>
                <div role="main"><p>Role main content</p></div>
                <article><p>Article content</p></article>
            </body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        val content = result.getOrThrow()
        assertEquals(listOf("Article content"), content.paragraphs)
    }

    @Test
    fun `extracts standalone links not inside paragraphs`() = runTest {
        val html = """
            <html><head><title>Article</title></head>
            <body><article>
                <p>See <a href="https://example.com/inline">inline link</a> in this paragraph.</p>
                <a href="https://example.com/standalone1">Standalone Link 1</a>
                <a href="https://example.com/standalone2">Standalone Link 2</a>
            </article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val content = jsoupArticleContentFetcher().fetch(server.url("/").toString()).getOrThrow()

        // Inline links (inside <p>) are excluded to avoid duplicating paragraph text
        assertEquals(listOf("Standalone Link 1" to "https://example.com/standalone1", "Standalone Link 2" to "https://example.com/standalone2"), content.links.map { it.text to it.url })
    }

    @Test
    fun `extracts image URLs from article element`() = runTest {
        val html = """
            <html><head><title>My Article</title></head>
            <body><article>
                <p>First para</p>
                <img src="https://example.com/photo.jpg" alt="A photo"/>
                <p>Second para</p>
                <img src="https://example.com/chart.png" alt="A chart"/>
            </article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val content = jsoupArticleContentFetcher().fetch(server.url("/").toString()).getOrThrow()

        assertEquals(listOf("https://example.com/photo.jpg", "https://example.com/chart.png"), content.imageUrls)
    }

    @Test
    fun `extracts title and paragraphs from article element`() = runTest {
        val html = """
            <html><head><title>My Article</title></head>
            <body><article><p>First para</p><p>Second para</p></article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val result = jsoupArticleContentFetcher().fetch(server.url("/").toString())

        assertTrue(result.isSuccess)
        val content = result.getOrThrow()
        assertEquals("My Article", content.title)
        assertEquals(listOf("First para", "Second para"), content.paragraphs)
    }
}

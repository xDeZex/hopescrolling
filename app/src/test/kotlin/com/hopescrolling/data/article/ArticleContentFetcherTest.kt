package com.hopescrolling.data.article

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.hopescrolling.data.article.ContentItem

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
    fun `accepts injected dispatcher`() = runTest {
        server.enqueue(MockResponse().setBody("<html><body><article><p>Hello</p></article></body></html>").setResponseCode(200))
        val result = jsoupArticleContentFetcher(UnconfinedTestDispatcher(testScheduler)).fetch(server.url("/").toString())
        assertTrue(result.isSuccess)
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
        assertTrue(content.items.filterIsInstance<ContentItem.Paragraph>().any { it.text == longText })
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
        assertEquals(listOf(ContentItem.Paragraph("Main content")), content.items)
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
        assertEquals(listOf(ContentItem.Paragraph("Main role content")), content.items)
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
        assertEquals(listOf(ContentItem.Paragraph("Article content")), content.items)
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
        assertEquals(
            listOf(
                ContentItem.Paragraph("See inline link in this paragraph."),
                ContentItem.Link("Standalone Link 1", "https://example.com/standalone1"),
                ContentItem.Link("Standalone Link 2", "https://example.com/standalone2"),
            ),
            content.items,
        )
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

        assertEquals(
            listOf(
                ContentItem.Paragraph("First para"),
                ContentItem.Image("https://example.com/photo.jpg"),
                ContentItem.Paragraph("Second para"),
                ContentItem.Image("https://example.com/chart.png"),
            ),
            content.items,
        )
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
        assertEquals(listOf(ContentItem.Paragraph("First para"), ContentItem.Paragraph("Second para")), content.items)
    }

    @Test
    fun `preserves document order of paragraphs images and links`() = runTest {
        val html = """
            <html><body><article>
                <p>First para</p>
                <a href="https://example.com/link1">A Link</a>
                <p>Second para</p>
            </article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val content = jsoupArticleContentFetcher().fetch(server.url("/").toString()).getOrThrow()

        assertEquals(
            listOf(
                ContentItem.Paragraph("First para"),
                ContentItem.Link("A Link", "https://example.com/link1"),
                ContentItem.Paragraph("Second para"),
            ),
            content.items,
        )
    }

    @Test
    fun `does not emit link for image-wrapping anchor`() = runTest {
        val html = """
            <html><body><article>
                <p>Before</p>
                <a href="https://example.com/photo"><img src="https://example.com/photo.jpg"/></a>
                <p>After</p>
            </article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val content = jsoupArticleContentFetcher().fetch(server.url("/").toString()).getOrThrow()

        assertEquals(
            listOf(
                ContentItem.Paragraph("Before"),
                ContentItem.Image("https://example.com/photo.jpg"),
                ContentItem.Paragraph("After"),
            ),
            content.items,
        )
    }

    @Test
    fun `preserves document order of paragraphs and images`() = runTest {
        val html = """
            <html><body><article>
                <p>First para</p>
                <img src="https://example.com/photo.jpg"/>
                <p>Second para</p>
            </article></body></html>
        """.trimIndent()
        server.enqueue(MockResponse().setBody(html).setResponseCode(200))

        val content = jsoupArticleContentFetcher().fetch(server.url("/").toString()).getOrThrow()

        assertEquals(
            listOf(
                ContentItem.Paragraph("First para"),
                ContentItem.Image("https://example.com/photo.jpg"),
                ContentItem.Paragraph("Second para"),
            ),
            content.items,
        )
    }
}

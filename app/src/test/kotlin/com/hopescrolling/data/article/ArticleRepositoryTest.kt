package com.hopescrolling.data.article

import com.hopescrolling.data.feed.FeedSource
import com.hopescrolling.util.FakeFeedSourceRepository
import com.hopescrolling.util.FakeRssFetcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleRepositoryTest {
    private val feedRepo = FakeFeedSourceRepository()

    @Test
    fun `single feed returns its articles`() = runTest {
        feedRepo.sources.value = listOf(FeedSource("a", "Feed A", "http://a.com/rss"))
        val xml = rss2Xml(Triple("Hello", "http://a.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"))
        val repo = DefaultArticleRepository(feedRepo, FakeRssFetcher(mapOf("http://a.com/rss" to xml)))
        val articles = repo.getArticles()
        assertEquals(1, articles.size)
        assertEquals("Hello", articles[0].title)
    }

    @Test
    fun `multiple feeds are merged`() = runTest {
        feedRepo.sources.value = listOf(
            FeedSource("a", "Feed A", "http://a.com/rss"),
            FeedSource("b", "Feed B", "http://b.com/rss"),
        )
        val xmlA = rss2Xml(Triple("From A", "http://a.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"))
        val xmlB = rss2Xml(Triple("From B", "http://b.com/1", "Tue, 02 Jan 2024 00:00:00 +0000"))
        val repo = DefaultArticleRepository(
            feedRepo,
            FakeRssFetcher(mapOf("http://a.com/rss" to xmlA, "http://b.com/rss" to xmlB)),
        )
        val articles = repo.getArticles()
        assertEquals(2, articles.size)
        val titles = articles.map { it.title }.toSet()
        assertEquals(setOf("From A", "From B"), titles)
    }

    @Test
    fun `articles are sorted newest first`() = runTest {
        feedRepo.sources.value = listOf(FeedSource("a", "Feed A", "http://a.com/rss"))
        val xml = rss2Xml(
            Triple("Old Article", "http://a.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"),
            Triple("New Article", "http://a.com/2", "Wed, 03 Jan 2024 00:00:00 +0000"),
            Triple("Mid Article", "http://a.com/3", "Tue, 02 Jan 2024 00:00:00 +0000"),
        )
        val repo = DefaultArticleRepository(feedRepo, FakeRssFetcher(mapOf("http://a.com/rss" to xml)))
        val articles = repo.getArticles()
        assertEquals(3, articles.size)
        assertEquals("New Article", articles[0].title)
        assertEquals("Mid Article", articles[1].title)
        assertEquals("Old Article", articles[2].title)
    }

    @Test
    fun `duplicate links are deduplicated`() = runTest {
        feedRepo.sources.value = listOf(
            FeedSource("a", "Feed A", "http://a.com/rss"),
            FeedSource("b", "Feed B", "http://b.com/rss"),
        )
        // Both feeds have the same article link
        val xmlA = rss2Xml(Triple("Article", "http://shared.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"))
        val xmlB = rss2Xml(Triple("Article", "http://shared.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"))
        val repo = DefaultArticleRepository(
            feedRepo,
            FakeRssFetcher(mapOf("http://a.com/rss" to xmlA, "http://b.com/rss" to xmlB)),
        )
        val articles = repo.getArticles()
        assertEquals(1, articles.size)
    }

    @Test
    fun `failing feed does not prevent other feeds from returning articles`() = runTest {
        feedRepo.sources.value = listOf(
            FeedSource("a", "Feed A", "http://a.com/rss"),
            FeedSource("bad", "Bad Feed", "http://bad.com/rss"),
        )
        val xmlA = rss2Xml(Triple("Good Article", "http://a.com/1", "Mon, 01 Jan 2024 00:00:00 +0000"))
        // "http://bad.com/rss" has no entry in the map, so FakeRssFetcher will throw
        val repo = DefaultArticleRepository(
            feedRepo,
            FakeRssFetcher(mapOf("http://a.com/rss" to xmlA)),
        )
        val articles = repo.getArticles()
        assertEquals(1, articles.size)
        assertEquals("Good Article", articles[0].title)
    }

    private fun rss2Xml(vararg items: Triple<String, String, String>): String {
        val itemsXml = items.joinToString("\n") { (title, link, pubDate) ->
            "<item><title>$title</title><link>$link</link><pubDate>$pubDate</pubDate></item>"
        }
        return """<?xml version="1.0"?><rss version="2.0"><channel>$itemsXml</channel></rss>"""
    }
}

package com.hopescrolling.data.article

import com.hopescrolling.data.feed.FeedSourceRepository
import com.hopescrolling.data.rss.Article
import com.hopescrolling.data.rss.RssParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale

class DefaultArticleRepository(
    private val feedSourceRepository: FeedSourceRepository,
    private val fetcher: RssFeedFetcher,
) : ArticleRepository {
    override suspend fun getArticles(): List<Article> = coroutineScope {
        feedSourceRepository.getAll().first()
            .map { source -> async { runCatching { fetcher.fetch(source.url).let { RssParser.parse(it, source.id) } }.getOrElse { emptyList() } } }
            .awaitAll()
            .flatten()
            .sortedByDescending { parsePubDate(it.pubDate) }
            .distinctBy { it.link }
    }

    private fun parsePubDate(pubDate: String?): Instant {
        if (pubDate == null) return Instant.EPOCH
        // Try ISO 8601
        runCatching { return Instant.parse(pubDate) }
        // Try RFC 822
        val rfc822Formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "dd MMM yyyy HH:mm:ss Z",
        )
        for (fmt in rfc822Formats) {
            runCatching {
                return SimpleDateFormat(fmt, Locale.ENGLISH).apply { isLenient = false }.parse(pubDate)!!.toInstant()
            }
        }
        return Instant.EPOCH
    }
}

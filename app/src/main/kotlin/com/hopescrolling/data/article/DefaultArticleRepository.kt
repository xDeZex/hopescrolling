package com.hopescrolling.data.article

import com.hopescrolling.data.feed.FeedSourceRepository
import com.hopescrolling.data.rss.Article
import com.hopescrolling.data.rss.DefaultRssParser
import com.hopescrolling.data.rss.RssParser
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class DefaultArticleRepository(
    private val feedSourceRepository: FeedSourceRepository,
    private val fetcher: RssFeedFetcher,
    private val parser: RssParser = DefaultRssParser,
) : ArticleRepository {
    override suspend fun getArticles(): List<Article> = coroutineScope {
        feedSourceRepository.getAll().first()
            .map { source ->
                async {
                    runCatching {
                        parser.parse(fetcher.fetch(source.url), source.id)
                            .map { it.copy(sourceName = source.name) }
                    }.getOrElse { emptyList() }
                }
            }
            .awaitAll()
            .flatten()
            .sortedByDescending { parsePubDate(it.pubDate) }
            .distinctBy { it.link }
    }
}

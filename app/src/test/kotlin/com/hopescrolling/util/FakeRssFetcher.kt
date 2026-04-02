package com.hopescrolling.util

import com.hopescrolling.data.article.RssFeedFetcher

class FakeRssFetcher(private val responses: Map<String, String>) : RssFeedFetcher {
    override suspend fun fetch(url: String) = responses[url] ?: error("No response for $url")
}

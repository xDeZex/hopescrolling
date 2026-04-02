package com.hopescrolling.data.article

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

fun interface RssFeedFetcher {
    suspend fun fetch(url: String): String
}

fun httpRssFeedFetcher(): RssFeedFetcher = RssFeedFetcher { url ->
    withContext(Dispatchers.IO) { URL(url).readText() }
}

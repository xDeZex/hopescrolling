package com.hopescrolling.data.article

fun interface RssFeedFetcher {
    suspend fun fetch(url: String): String
}

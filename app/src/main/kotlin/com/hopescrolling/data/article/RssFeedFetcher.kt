package com.hopescrolling.data.article

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

fun interface RssFeedFetcher {
    suspend fun fetch(url: String): String
}

fun httpRssFeedFetcher(dispatcher: CoroutineDispatcher = Dispatchers.IO): RssFeedFetcher = RssFeedFetcher { url ->
    withContext(dispatcher) {
        fetchFollowingRedirects(url, remainingRedirects = 5)
    }
}

private fun fetchFollowingRedirects(url: String, remainingRedirects: Int): String {
    if (remainingRedirects <= 0) throw IOException("Too many redirects for $url")
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = false
    connection.connectTimeout = 10_000
    connection.readTimeout = 30_000
    return try {
        val responseCode = connection.responseCode
        when {
            responseCode in 300..399 -> {
                val location = connection.getHeaderField("Location")
                    ?: throw IOException("Redirect with no Location header for $url")
                fetchFollowingRedirects(URL(URL(url), location).toString(), remainingRedirects - 1)
            }
            responseCode !in 200..299 -> throw IOException("HTTP $responseCode for $url")
            else -> connection.inputStream.bufferedReader().readText()
        }
    } finally {
        connection.disconnect()
    }
}

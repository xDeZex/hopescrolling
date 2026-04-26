package com.hopescrolling.util

import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.rss.Article
import kotlinx.coroutines.suspendCancellableCoroutine

class FakeArticleRepository(
    private val articles: List<Article> = emptyList(),
    error: Throwable? = null,
    private val hangAfterCalls: Int = Int.MAX_VALUE,
) : ArticleRepository {
    var callCount = 0
        private set

    private var error: Throwable? = error

    fun clearError() {
        error = null
    }

    override suspend fun getArticles(): List<Article> {
        callCount++
        if (callCount > hangAfterCalls) suspendCancellableCoroutine<Nothing> { }
        error?.let { throw it }
        return articles
    }
}

package com.hopescrolling.util

import com.hopescrolling.data.article.ArticleRepository
import com.hopescrolling.data.rss.Article

class FakeArticleRepository(
    private val articles: List<Article> = emptyList(),
    private val error: Throwable? = null,
) : ArticleRepository {
    var callCount = 0
        private set

    override suspend fun getArticles(): List<Article> {
        callCount++
        if (error != null) throw error
        return articles
    }
}

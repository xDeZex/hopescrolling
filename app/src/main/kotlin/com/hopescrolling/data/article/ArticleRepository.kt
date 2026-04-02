package com.hopescrolling.data.article

import com.hopescrolling.data.rss.Article

interface ArticleRepository {
    suspend fun getArticles(): List<Article>
}

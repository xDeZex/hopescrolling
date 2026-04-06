package com.hopescrolling.util

import com.hopescrolling.data.article.ArticleContent
import com.hopescrolling.data.article.ArticleContentFetcher

class FakeArticleContentFetcher(
    private val result: Result<ArticleContent> = Result.success(
        ArticleContent(title = "Test Title", paragraphs = listOf("Test paragraph")),
    ),
) : ArticleContentFetcher {
    override suspend fun fetch(url: String): Result<ArticleContent> = result
}

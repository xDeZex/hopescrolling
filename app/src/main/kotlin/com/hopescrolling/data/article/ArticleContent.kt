package com.hopescrolling.data.article

data class ArticleLink(val text: String, val url: String)

data class ArticleContent(
    val title: String,
    val paragraphs: List<String>,
    val imageUrls: List<String> = emptyList(),
    val links: List<ArticleLink> = emptyList(),
)

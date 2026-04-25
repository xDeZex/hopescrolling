package com.hopescrolling.data.article

data class ArticleLink(val text: String, val url: String)

sealed class ContentItem {
    data class Paragraph(val text: String) : ContentItem()
    data class Image(val url: String) : ContentItem()
}

data class ArticleContent(
    val title: String,
    val items: List<ContentItem> = emptyList(),
    val links: List<ArticleLink> = emptyList(),
)

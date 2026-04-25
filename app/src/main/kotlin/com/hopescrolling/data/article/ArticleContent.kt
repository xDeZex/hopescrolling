package com.hopescrolling.data.article

sealed class ContentItem {
    data class Paragraph(val text: String) : ContentItem()
    data class Image(val url: String) : ContentItem()
    data class Link(val text: String, val url: String) : ContentItem()
}

data class ArticleContent(
    val title: String,
    val items: List<ContentItem> = emptyList(),
)

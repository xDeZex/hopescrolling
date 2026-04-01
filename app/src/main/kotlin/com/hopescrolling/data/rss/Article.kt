package com.hopescrolling.data.rss

data class Article(
    val title: String,
    val link: String,
    val description: String?,
    val pubDate: String?,
    val feedSourceId: String
)

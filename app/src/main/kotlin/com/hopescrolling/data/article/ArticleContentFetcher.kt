package com.hopescrolling.data.article

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

interface ArticleContentFetcher {
    suspend fun fetch(url: String): Result<ArticleContent>
}

fun jsoupArticleContentFetcher(dispatcher: CoroutineDispatcher = Dispatchers.IO): ArticleContentFetcher =
    JsoupArticleContentFetcher(dispatcher)

private class JsoupArticleContentFetcher(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArticleContentFetcher {
    override suspend fun fetch(url: String): Result<ArticleContent> = withContext(dispatcher) {
        runCatching {
            val html = fetchHtml(url)
            parseContent(html, url)
        }
    }

    private fun fetchHtml(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 30_000
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) throw IOException("HTTP $responseCode")
            return connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseContent(html: String, url: String): ArticleContent {
        val doc = Jsoup.parse(html, url)
        val title = doc.title()
        val contentEl = findContentElement(doc)
        val items = buildList {
            for (el in contentEl.select("p, img[src]")) {
                when (el.tagName()) {
                    "p" -> {
                        val text = el.text()
                        if (text.isNotBlank()) add(ContentItem.Paragraph(text))
                    }
                    "img" -> {
                        val src = el.absUrl("src")
                        if (src.isNotBlank()) add(ContentItem.Image(src))
                    }
                }
            }
        }
        // Only extract links NOT inside <p> elements — paragraph text already contains their
        // visible text, so including them here would duplicate content for the user.
        val links = contentEl.select("a[href]")
            .filter { el -> el.parents().none { it.tagName() == "p" } }
            .mapNotNull { el ->
                val text = el.text().trim()
                val href = el.absUrl("href")
                if (text.isNotBlank() && href.isNotBlank()) ArticleLink(text, href) else null
            }
        return ArticleContent(title = title, items = items, links = links)
    }

    private fun findContentElement(doc: Document): Element =
        doc.selectFirst("article")
            ?: doc.selectFirst("[role=main]")
            ?: doc.selectFirst("main")
            ?: doc.select("div").maxByOrNull { it.text().length }
            ?: doc.body()
}

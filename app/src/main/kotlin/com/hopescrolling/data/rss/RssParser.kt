package com.hopescrolling.data.rss

import org.jsoup.Jsoup
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

object RssParser {
    fun parse(xml: String, feedSourceId: String): List<Article> {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(xml.byteInputStream())
        doc.documentElement.normalize()

        val root = doc.documentElement
        return when (root.tagName) {
            "rss" -> parseRss2(root, feedSourceId)
            "feed" -> parseAtom(root, feedSourceId)
            else -> emptyList()
        }
    }

    private fun parseRss2(root: Element, feedSourceId: String): List<Article> =
        root.getElementsByTagName("item").toList().map { item ->
            Article(
                title = item.text("title") ?: "",
                link = item.rawText("link") ?: "",
                description = item.text("description"),
                pubDate = item.rawText("pubDate"),
                feedSourceId = feedSourceId,
            )
        }

    private fun parseAtom(root: Element, feedSourceId: String): List<Article> =
        root.getElementsByTagName("entry").toList().map { entry ->
            Article(
                title = entry.text("title") ?: "",
                link = entry.attr("link", "href") ?: "",
                description = entry.text("summary") ?: entry.text("content"),
                pubDate = entry.rawText("published") ?: entry.rawText("updated"),
                feedSourceId = feedSourceId,
            )
        }

    private fun NodeList.toList(): List<Element> =
        (0 until length).map { item(it) as Element }

    private fun Element.text(tag: String): String? =
        getElementsByTagName(tag).item(0)?.textContent
            ?.let { sanitize(it) }
            ?.takeIf { it.isNotEmpty() }

    private fun Element.rawText(tag: String): String? =
        getElementsByTagName(tag).item(0)?.textContent?.trim()?.takeIf { it.isNotEmpty() }

    private fun Element.attr(tag: String, attr: String): String? =
        (getElementsByTagName(tag).item(0) as? Element)?.getAttribute(attr)?.takeIf { it.isNotEmpty() }

    private fun sanitize(text: String): String = Jsoup.parseBodyFragment(text).text()
}

package com.hopescrolling.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RssParserTest {

    // ── Behavior 1: minimal RSS 2.0 ──────────────────────────────────────────

    @Test
    fun parse_minimalRss2Feed_returnsOneArticleWithCorrectFields() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test Feed</title>
                <item>
                  <title>Hello World</title>
                  <link>https://example.com/1</link>
                  <description>A test article</description>
                  <pubDate>Mon, 01 Jan 2024 00:00:00 +0000</pubDate>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals(1, articles.size)
        val a = articles[0]
        assertEquals("Hello World", a.title)
        assertEquals("https://example.com/1", a.link)
        assertEquals("A test article", a.description)
        assertEquals("Mon, 01 Jan 2024 00:00:00 +0000", a.pubDate)
        assertEquals("feed-1", a.feedSourceId)
    }

    // ── Behavior 2: link field is NOT sanitized ───────────────────────────────

    @Test
    fun parse_linkField_isNotSanitized() {
        // A link containing an angle-bracketed segment looks like an HTML tag to Jsoup.
        // With the current (broken) code, sanitize() strips <path> leaving "https://example.com/".
        // After the fix, rawText() is used for link so the value is preserved as-is.
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Test</title>
                  <link><![CDATA[https://example.com/<path>]]></link>
                  <description>desc</description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("https://example.com/<path>", articles[0].link)
    }

    // ── Behavior 5: CDATA numeric entity in title field ───────────────────────

    @Test
    fun parse_cdataWithHtmlEntitiesInTitle_decodesEntities() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title><![CDATA[It&#8217;s a feature, not a bug]]></title>
                  <link>https://example.com/1</link>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("It\u2019s a feature, not a bug", articles[0].title)
    }
}

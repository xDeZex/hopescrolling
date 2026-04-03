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

    // ── Behavior 2: CDATA numeric HTML entities ───────────────────────────────

    @Test
    fun parse_cdataWithNumericHtmlEntities_decodesEntities() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Test</title>
                  <link>https://example.com/1</link>
                  <description><![CDATA[Em dash &#8211; and &#8220;smart quotes&#8221;]]></description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("Em dash \u2013 and \u201csmart quotes\u201d", articles[0].description)
    }

    // ── Behavior 3: CDATA named HTML entities ─────────────────────────────────

    @Test
    fun parse_cdataWithNamedHtmlEntities_decodesEntities() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Test</title>
                  <link>https://example.com/1</link>
                  <description><![CDATA[A&amp;B &lt;tag&gt; &quot;quoted&quot; &apos;apos&apos; non&nbsp;breaking]]></description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("A&B <tag> \"quoted\" 'apos' non breaking", articles[0].description)
    }

    // ── Behavior 5: link field preserves raw value ────────────────────────────

    @Test
    fun parse_linkWithAngleBrackets_preservesRawValue() {
        // Jsoup strips <path> as an HTML tag; link fields must bypass sanitize()
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Test</title>
                  <link><![CDATA[https://example.com/<path>]]></link>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("https://example.com/<path>", articles[0].link)
    }

    // ── Behavior 6: numeric entity in title field ────────────────────────────

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

    // ── Behavior 4: CDATA HTML tags stripped ──────────────────────────────────

    @Test
    fun parse_cdataWithHtmlTags_stripsTagsToPlainText() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>Test</title>
                  <link>https://example.com/1</link>
                  <description><![CDATA[<p>First paragraph</p><p>Second paragraph</p>]]></description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val articles = RssParser.parse(xml, "feed-1")

        assertEquals("First paragraph Second paragraph", articles[0].description)
    }
}

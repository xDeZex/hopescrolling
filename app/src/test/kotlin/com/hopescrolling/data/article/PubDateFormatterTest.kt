package com.hopescrolling.data.article

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class PubDateFormatterTest {

    // Use a fixed "now" of 2026-06-15T10:00:00Z for all tests
    private val now: Instant = Instant.parse("2026-06-15T10:00:00Z")

    @Test
    fun formatPubDate_null_returnsNull() {
        assertNull(formatPubDate(null, now))
    }

    @Test
    fun formatPubDate_unparseable_returnsNull() {
        assertNull(formatPubDate("not a date at all", now))
    }

    @Test
    fun formatPubDate_sameYear_returnsMonthDay() {
        // "Mon, 01 Jan 2026 12:00:00 GMT" → same year as now (2026) → "Jan 1"
        val result = formatPubDate("Mon, 01 Jan 2026 12:00:00 GMT", now)
        assertEquals("Jan 1", result)
    }

    @Test
    fun formatPubDate_differentYear_returnsMonthDayYear() {
        // "Wed, 01 Jan 2025 12:00:00 GMT" → different year from now (2026) → "Jan 1, 2025"
        val result = formatPubDate("Wed, 01 Jan 2025 12:00:00 GMT", now)
        assertEquals("Jan 1, 2025", result)
    }

    @Test
    fun formatPubDate_iso8601SameYear_returnsMonthDay() {
        val result = formatPubDate("2026-03-15T08:30:00Z", now)
        assertEquals("Mar 15", result)
    }

    @Test
    fun formatPubDate_iso8601DifferentYear_returnsMonthDayYear() {
        val result = formatPubDate("2024-12-25T00:00:00Z", now)
        assertEquals("Dec 25, 2024", result)
    }

    @Test
    fun formatPubDate_epoch_returnsNull() {
        // A date string that parses to Instant.EPOCH triggers the sentinel check → null
        assertNull(formatPubDate("Thu, 01 Jan 1970 00:00:00 GMT", now))
    }
}

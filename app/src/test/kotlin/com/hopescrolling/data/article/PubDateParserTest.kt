package com.hopescrolling.data.article

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PubDateParserTest {

    @Test
    fun `ISO 8601 format parses correctly`() {
        val result = parsePubDate("2024-03-15T10:30:00Z")
        assertEquals(Instant.parse("2024-03-15T10:30:00Z"), result)
    }

    @Test
    fun `RFC 822 format with day of week parses correctly`() {
        val result = parsePubDate("Fri, 15 Mar 2024 10:30:00 +0000")
        assertEquals(Instant.parse("2024-03-15T10:30:00Z"), result)
    }

    @Test
    fun `RFC 822 format without day of week parses correctly`() {
        val result = parsePubDate("15 Mar 2024 10:30:00 +0000")
        assertEquals(Instant.parse("2024-03-15T10:30:00Z"), result)
    }

    @Test
    fun `null input returns Instant EPOCH`() {
        assertEquals(Instant.EPOCH, parsePubDate(null))
    }

    @Test
    fun `unrecognized format returns Instant EPOCH`() {
        assertEquals(Instant.EPOCH, parsePubDate("not a date at all"))
    }

    @Test
    fun `newer date parses to greater Instant than older date`() {
        val older = parsePubDate("Mon, 01 Jan 2024 00:00:00 +0000")
        val newer = parsePubDate("Wed, 01 Jan 2025 00:00:00 +0000")
        assertTrue("newer ($newer) should be after older ($older)", newer > older)
    }
}

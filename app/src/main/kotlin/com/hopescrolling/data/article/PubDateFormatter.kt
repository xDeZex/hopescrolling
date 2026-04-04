package com.hopescrolling.data.article

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sameYearFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH).withZone(ZoneOffset.UTC)

private val differentYearFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH).withZone(ZoneOffset.UTC)

/**
 * Formats a raw RSS pubDate string into a concise human-readable string.
 *
 * - Returns `null` if [pubDate] is null or cannot be parsed.
 * - Returns `"Jan 1"` if the date is in the same year as [now].
 * - Returns `"Jan 1, 2025"` if the date is in a different year.
 */
fun formatPubDate(pubDate: String?, now: Instant = Instant.now()): String? {
    val instant = parsePubDate(pubDate)
    if (instant == Instant.EPOCH) return null

    val dateYear = instant.atZone(ZoneOffset.UTC).year
    val nowYear = now.atZone(ZoneOffset.UTC).year

    return if (dateYear == nowYear) {
        sameYearFormatter.format(instant)
    } else {
        differentYearFormatter.format(instant)
    }
}

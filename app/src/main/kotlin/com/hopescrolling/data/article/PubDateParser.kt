package com.hopescrolling.data.article

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale

fun parsePubDate(pubDate: String?): Instant {
    if (pubDate == null) return Instant.EPOCH
    // Try ISO 8601
    runCatching { return Instant.parse(pubDate) }
    // Try RFC 822 / RSS common variants (Z for numeric offsets, zzz for named zones like GMT)
    val rfc822Formats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss Z",
        "dd MMM yyyy HH:mm:ss zzz",
    )
    for (fmt in rfc822Formats) {
        runCatching {
            // isLenient=true (default) is intentional: RSS feeds often include an incorrect
            // day-of-week or a named timezone (e.g. "GMT") that strict mode rejects.
            return SimpleDateFormat(fmt, Locale.ENGLISH).parse(pubDate)!!.toInstant()
        }
    }
    return Instant.EPOCH
}

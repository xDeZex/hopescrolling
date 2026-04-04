package com.hopescrolling.data.article

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale

fun parsePubDate(pubDate: String?): Instant {
    if (pubDate == null) return Instant.EPOCH
    // Try ISO 8601
    runCatching { return Instant.parse(pubDate) }
    // Try RFC 822
    val rfc822Formats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "dd MMM yyyy HH:mm:ss Z",
    )
    for (fmt in rfc822Formats) {
        runCatching {
            return SimpleDateFormat(fmt, Locale.ENGLISH).apply { isLenient = false }.parse(pubDate)!!.toInstant()
        }
    }
    return Instant.EPOCH
}

package com.hopescrolling.data.update

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class HttpAppUpdateRepository(
    private val apiUrl: String,
    private val currentVersionCode: Int,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AppUpdateRepository {

    override suspend fun getUpdateState(): UpdateState = withContext(dispatcher) {
        try {
            val json = fetch(apiUrl)
            val releases = JSONArray(json)

            var maxVersionCode = -1
            var latestLabel = ""
            var apkUrl = ""

            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                val tagName = release.getString("tag_name")
                val versionCode = tagName.removePrefix("build-").toIntOrNull() ?: continue
                if (versionCode <= maxVersionCode) continue
                val assets = release.getJSONArray("assets")
                val url = (0 until assets.length())
                    .map { assets.getJSONObject(it) }
                    .firstOrNull { it.getString("name").endsWith(".apk") }
                    ?.getString("browser_download_url")
                    ?: continue
                maxVersionCode = versionCode
                latestLabel = release.getString("name")
                apkUrl = url
            }

            if (maxVersionCode > currentVersionCode) {
                UpdateState.UpdateAvailable(latestLabel, apkUrl)
            } else {
                UpdateState.UpToDate
            }
        } catch (e: Exception) {
            UpdateState.Error
        }
    }

    private fun fetch(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 30_000
        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) throw Exception("HTTP $responseCode")
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}

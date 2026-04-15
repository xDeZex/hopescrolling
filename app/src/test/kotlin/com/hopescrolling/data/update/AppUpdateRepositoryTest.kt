package com.hopescrolling.data.update

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppUpdateRepositoryTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `returns Error on non-2xx response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 1,
        )
        assertEquals(UpdateState.Error, repo.getUpdateState())
    }

    @Test
    fun `returns UpToDate when API has no release with higher version code`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-1","name":"build-1","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 5,
        )
        assertEquals(UpdateState.UpToDate, repo.getUpdateState())
    }

    @Test
    fun `returns UpdateAvailable when API has release with higher version code`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-5","name":"build-5","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 1,
        )
        assertEquals(
            UpdateState.UpdateAvailable("build-5", "https://example.com/app.apk"),
            repo.getUpdateState(),
        )
    }

    @Test
    fun `returns UpToDate when release has no APK asset`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-5","name":"build-5","assets":[]}]"""
            )
        )
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 1,
        )
        assertEquals(UpdateState.UpToDate, repo.getUpdateState())
    }

    @Test
    fun `returns UpToDate when releases array is empty`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 1,
        )
        assertEquals(UpdateState.UpToDate, repo.getUpdateState())
    }

    @Test
    fun `ignores releases with non-build tag names`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"v1.0.0","name":"v1.0.0","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        val repo = HttpAppUpdateRepository(
            apiUrl = server.url("/releases").toString(),
            currentVersionCode = 1,
        )
        assertEquals(UpdateState.UpToDate, repo.getUpdateState())
    }
}

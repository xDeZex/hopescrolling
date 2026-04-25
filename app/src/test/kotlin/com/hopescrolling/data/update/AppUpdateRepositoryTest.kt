package com.hopescrolling.data.update

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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

    private fun TestScope.repo(currentVersionCode: Int) = HttpAppUpdateRepository(
        apiUrl = server.url("/releases").toString(),
        currentVersionCode = currentVersionCode,
        dispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    @Test
    fun `returns Error on non-2xx response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        assertEquals(UpdateState.Error, repo(1).getUpdateState())
    }

    @Test
    fun `returns UpToDate when API has no release with higher version code`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-1","name":"build-1","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        assertEquals(UpdateState.UpToDate, repo(5).getUpdateState())
    }

    @Test
    fun `returns UpdateAvailable when API has release with higher version code`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-5","name":"build-5","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        assertEquals(
            UpdateState.UpdateAvailable("build-5", "https://example.com/app.apk"),
            repo(1).getUpdateState(),
        )
    }

    @Test
    fun `returns UpToDate when release has no APK asset`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"build-5","name":"build-5","assets":[]}]"""
            )
        )
        assertEquals(UpdateState.UpToDate, repo(1).getUpdateState())
    }

    @Test
    fun `returns UpToDate when releases array is empty`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        assertEquals(UpdateState.UpToDate, repo(1).getUpdateState())
    }

    @Test
    fun `ignores releases with non-build tag names`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"tag_name":"v1.0.0","name":"v1.0.0","assets":[{"name":"app-debug.apk","browser_download_url":"https://example.com/app.apk"}]}]"""
            )
        )
        assertEquals(UpdateState.UpToDate, repo(1).getUpdateState())
    }
}

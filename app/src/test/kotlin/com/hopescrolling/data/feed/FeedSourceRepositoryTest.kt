package com.hopescrolling.data.feed

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeedSourceRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun createRepository(): FeedSourceRepository {
        val dataStore = PreferenceDataStoreFactory.create {
            temporaryFolder.newFile("test.preferences_pb")
        }
        return DataStoreFeedSourceRepository(dataStore)
    }

    @Test
    fun `fresh repository emits empty list`() = runTest {
        val repo = createRepository()
        val result = repo.getAll().first()
        assertEquals(emptyList<FeedSource>(), result)
    }

    @Test
    fun `add a source and getAll emits list containing that source`() = runTest {
        val repo = createRepository()
        val source = FeedSource(id = "1", name = "Test Feed", url = "https://example.com/feed")
        repo.add(source)
        val result = repo.getAll().first()
        assertEquals(listOf(source), result)
    }

    @Test
    fun `remove an added source and getAll no longer contains it`() = runTest {
        val repo = createRepository()
        val source = FeedSource(id = "1", name = "Test Feed", url = "https://example.com/feed")
        repo.add(source)
        repo.remove(source.id)
        val result = repo.getAll().first()
        assertEquals(emptyList<FeedSource>(), result)
    }

    @Test
    fun `add two sources remove one only the other remains`() = runTest {
        val repo = createRepository()
        val source1 = FeedSource(id = "1", name = "Feed One", url = "https://example.com/feed1")
        val source2 = FeedSource(id = "2", name = "Feed Two", url = "https://example.com/feed2")
        repo.add(source1)
        repo.add(source2)
        repo.remove(source1.id)
        val result = repo.getAll().first()
        assertEquals(listOf(source2), result)
    }
}

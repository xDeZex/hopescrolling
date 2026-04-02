package com.hopescrolling.data.feed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreFeedSourceRepository(
    private val dataStore: DataStore<Preferences>
) : FeedSourceRepository {

    private val SOURCE_IDS = stringSetPreferencesKey("source_ids")

    private fun nameKey(id: String) = stringPreferencesKey("source_name_$id")
    private fun urlKey(id: String) = stringPreferencesKey("source_url_$id")

    override fun getAll(): Flow<List<FeedSource>> {
        return dataStore.data.map { prefs ->
            val ids = prefs[SOURCE_IDS] ?: emptySet()
            ids.mapNotNull { id ->
                val name = prefs[nameKey(id)] ?: return@mapNotNull null
                val url = prefs[urlKey(id)] ?: return@mapNotNull null
                FeedSource(id = id, name = name, url = url)
            }
        }
    }

    override suspend fun add(source: FeedSource) {
        dataStore.edit { prefs ->
            val ids = prefs[SOURCE_IDS]?.toMutableSet() ?: mutableSetOf()
            ids.add(source.id)
            prefs[SOURCE_IDS] = ids
            prefs[nameKey(source.id)] = source.name
            prefs[urlKey(source.id)] = source.url
        }
    }

    override suspend fun remove(id: String) {
        dataStore.edit { prefs ->
            val ids = prefs[SOURCE_IDS]?.toMutableSet() ?: mutableSetOf()
            ids.remove(id)
            prefs[SOURCE_IDS] = ids
            prefs.remove(nameKey(id))
            prefs.remove(urlKey(id))
        }
    }

    override suspend fun update(source: FeedSource) {
        dataStore.edit { prefs ->
            prefs[nameKey(source.id)] = source.name
            prefs[urlKey(source.id)] = source.url
        }
    }
}

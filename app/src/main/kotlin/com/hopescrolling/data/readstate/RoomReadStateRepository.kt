package com.hopescrolling.data.readstate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReadStateRepository(private val dao: ReadArticleDao) : ReadStateRepository {
    override fun getReadIds(): Flow<Set<String>> = dao.getAllIds().map { it.toSet() }

    override suspend fun markRead(articleId: String) {
        dao.insert(ReadArticleEntity(id = articleId))
    }
}

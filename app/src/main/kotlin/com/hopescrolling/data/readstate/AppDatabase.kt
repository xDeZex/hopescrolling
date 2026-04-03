package com.hopescrolling.data.readstate

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReadArticleEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readArticleDao(): ReadArticleDao
}

package com.hopescrolling.data.readstate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_articles")
data class ReadArticleEntity(
    @PrimaryKey val id: String,
)

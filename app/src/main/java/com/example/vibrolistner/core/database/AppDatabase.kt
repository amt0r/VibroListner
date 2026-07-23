package com.example.vibrolistner.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [KeywordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keywordDao(): KeywordDao
}

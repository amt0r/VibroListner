package com.example.vibrolistner.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordDao {

    @Query("SELECT * FROM keywords ORDER BY id DESC")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: KeywordEntity)

    @Delete
    suspend fun deleteKeyword(keyword: KeywordEntity)
}

package com.example.vibrolistner.core.data

import com.example.vibrolistner.core.database.KeywordDao
import com.example.vibrolistner.core.database.KeywordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordRepository @Inject constructor(
    private val keywordDao: KeywordDao,
) {
    fun getAllKeywords(): Flow<List<KeywordEntity>> = keywordDao.getAllKeywords()

    suspend fun insertKeyword(keyword: String) {
        keywordDao.insertKeyword(KeywordEntity(keyword = keyword))
    }

    suspend fun deleteKeyword(keyword: KeywordEntity) {
        keywordDao.deleteKeyword(keyword)
    }
}

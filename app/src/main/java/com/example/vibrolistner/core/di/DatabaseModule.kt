package com.example.vibrolistner.core.di

import android.content.Context
import androidx.room.Room
import com.example.vibrolistner.core.database.AppDatabase
import com.example.vibrolistner.core.database.KeywordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vibrolistner_database",
        ).build()
    }

    @Provides
    @Singleton
    fun provideKeywordDao(database: AppDatabase): KeywordDao {
        return database.keywordDao()
    }
}

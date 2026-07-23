package com.example.vibrolistner.core.di

import android.content.Context
import com.example.vibrolistner.core.vibration.VibrationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VibrationModule {

    @Provides
    @Singleton
    fun provideVibrationManager(@ApplicationContext context: Context): VibrationManager {
        return VibrationManager(context)
    }

    @Provides
    @Singleton
    fun provideAlertManager(
        @ApplicationContext context: Context,
        vibrationManager: VibrationManager
    ): com.example.vibrolistner.core.alert.AlertManager {
        return com.example.vibrolistner.core.alert.AlertManager(context, vibrationManager)
    }
}

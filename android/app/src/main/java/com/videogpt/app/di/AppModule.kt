package com.videogpt.app.di

import android.content.Context
import androidx.room.Room
import com.videogpt.app.ai.AiProvider
import com.videogpt.app.ai.LocalFallbackProvider
import com.videogpt.app.data.AppDatabase
import com.videogpt.app.engine.VideoCompositionEngine
import com.videogpt.app.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "videogpt_db").build()

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager =
        SettingsManager(context)

    @Provides
    @Singleton
    fun provideVideoEngine(@ApplicationContext context: Context): VideoCompositionEngine =
        VideoCompositionEngine(context)

    @Provides
    @Singleton
    fun provideAiProvider(): AiProvider = LocalFallbackProvider()
}

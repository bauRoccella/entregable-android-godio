package com.example.reactionchallenge.di

import android.content.Context
import androidx.room.Room
import com.example.reactionchallenge.data.local.AppDatabase
import com.example.reactionchallenge.data.local.dao.GameSessionDao
import com.example.reactionchallenge.data.local.dao.PlayerDao
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
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideGameSessionDao(db: AppDatabase): GameSessionDao = db.gameSessionDao()
}

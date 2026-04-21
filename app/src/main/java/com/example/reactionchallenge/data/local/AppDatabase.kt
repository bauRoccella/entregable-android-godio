package com.example.reactionchallenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.reactionchallenge.data.local.dao.GameSessionDao
import com.example.reactionchallenge.data.local.dao.PlayerDao
import com.example.reactionchallenge.data.local.entity.GameSessionEntity
import com.example.reactionchallenge.data.local.entity.PlayerEntity

@Database(
    entities = [PlayerEntity::class, GameSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameSessionDao(): GameSessionDao

    companion object {
        const val DATABASE_NAME = "reaction_challenge.db"
    }
}

package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val packageName: String,
    val category: String = "Action",
    val profileName: String = "ULTRA", // ULTRA, BALANCED, BATTERY, ESPORTS
    val playTimeMinutes: Int = 0,
    val targetFps: Int = 120,
    val touchBoostMultiplier: Float = 1.5f,
    val isFavorite: Boolean = true,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

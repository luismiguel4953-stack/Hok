package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastPlayedTimestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedTimestamp DESC")
    fun getFavoriteGames(): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("UPDATE games SET playTimeMinutes = playTimeMinutes + :addedMinutes, lastPlayedTimestamp = :now WHERE id = :gameId")
    suspend fun updatePlayTime(gameId: Long, addedMinutes: Int, now: Long = System.currentTimeMillis())
}

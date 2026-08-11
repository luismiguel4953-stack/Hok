package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BoostLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoostLogDao {
    @Query("SELECT * FROM boost_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentBoostLogs(): Flow<List<BoostLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BoostLogEntity)

    @Query("DELETE FROM boost_logs")
    suspend fun clearLogs()
}

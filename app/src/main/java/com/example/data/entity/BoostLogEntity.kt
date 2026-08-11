package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boost_logs")
data class BoostLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val memoryFreedMb: Int,
    val temperatureBefore: Float,
    val temperatureAfter: Float,
    val profileApplied: String
)

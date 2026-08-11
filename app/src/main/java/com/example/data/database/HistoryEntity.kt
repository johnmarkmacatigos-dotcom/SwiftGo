package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originName: String,
    val originLat: Double,
    val originLng: Double,
    val destinationName: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val category: String, // "RIDES", "FOOD", "DELIVERY"
    val timestamp: Long = System.currentTimeMillis(),
    val platformId: String, // e.g. "grab", "maxim", "xicar", etc.
    val fare: Double,
    val pointsEarned: Int = 15
)

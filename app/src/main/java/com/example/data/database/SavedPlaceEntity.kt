package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g. "Home", "Office", "SM City Iloilo"
    val address: String, // e.g. "Mandurriao, Iloilo City"
    val latitude: Double,
    val longitude: Double,
    val iconName: String // "home", "work", "star", "school", "shopping", "restaurant"
)

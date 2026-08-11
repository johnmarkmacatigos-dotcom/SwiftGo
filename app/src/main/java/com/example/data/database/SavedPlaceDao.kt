package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places")
    fun getAllSavedPlaces(): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlaces(places: List<SavedPlaceEntity>)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteSavedPlaceById(id: Int)

    @Query("SELECT COUNT(*) FROM saved_places")
    suspend fun getCount(): Int
}

package com.example.data.repository

import com.example.data.database.HistoryDao
import com.example.data.database.HistoryEntity
import com.example.data.database.SavedPlaceDao
import com.example.data.database.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class OneGoRepository(
    private val historyDao: HistoryDao,
    private val savedPlaceDao: SavedPlaceDao
) {
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    val totalPoints: Flow<Int?> = historyDao.getTotalPointsFlow()

    val allSavedPlaces: Flow<List<SavedPlaceEntity>> = savedPlaceDao.getAllSavedPlaces()
        .onStart {
            checkAndPrepopulateSavedPlaces()
        }

    suspend fun insertHistory(entry: HistoryEntity): Long {
        return historyDao.insertHistory(entry)
    }

    suspend fun deleteHistoryById(id: Int) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun insertSavedPlace(place: SavedPlaceEntity): Long {
        return savedPlaceDao.insertSavedPlace(place)
    }

    suspend fun deleteSavedPlaceById(id: Int) {
        savedPlaceDao.deleteSavedPlaceById(id)
    }

    private suspend fun checkAndPrepopulateSavedPlaces() {
        if (savedPlaceDao.getCount() == 0) {
            val defaultPlaces = listOf(
                SavedPlaceEntity(
                    name = "Home",
                    address = "Mandurriao, Iloilo City",
                    latitude = 10.7095,
                    longitude = 122.5484,
                    iconName = "home"
                ),
                SavedPlaceEntity(
                    name = "Office (Megaworld)",
                    address = "Megaworld Boulevard, Mandurriao, Iloilo City",
                    latitude = 10.7188,
                    longitude = 122.5434,
                    iconName = "work"
                ),
                SavedPlaceEntity(
                    name = "SM City Iloilo",
                    address = "Benigno Aquino Ave, Mandurriao, Iloilo City",
                    latitude = 10.7123,
                    longitude = 122.5518,
                    iconName = "shopping"
                ),
                SavedPlaceEntity(
                    name = "Iloilo Esplanade",
                    address = "Molo, Iloilo City",
                    latitude = 10.7011,
                    longitude = 122.5512,
                    iconName = "star"
                ),
                SavedPlaceEntity(
                    name = "CPU Campus",
                    address = "Lopez Jaena St, Jaro, Iloilo City",
                    latitude = 10.7247,
                    longitude = 122.5606,
                    iconName = "school"
                ),
                SavedPlaceEntity(
                    name = "Festive Walk Mall",
                    address = "Iloilo Business Park, Mandurriao, Iloilo City",
                    latitude = 10.7176,
                    longitude = 122.5422,
                    iconName = "restaurant"
                )
            )
            savedPlaceDao.insertSavedPlaces(defaultPlaces)
        }
    }
}

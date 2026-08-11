package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class, SavedPlaceEntity::class], version = 1, exportSchema = false)
abstract class OneGoDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun savedPlaceDao(): SavedPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: OneGoDatabase? = null

        fun getDatabase(context: Context): OneGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OneGoDatabase::class.java,
                    "onego_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

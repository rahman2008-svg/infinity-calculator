package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.HistoryItem
import com.example.data.model.CalcNote

@Database(entities = [HistoryItem::class, CalcNote::class], version = 1, exportSchema = false)
abstract class CalcDatabase : RoomDatabase() {
    abstract fun calcDao(): CalcDao

    companion object {
        @Volatile
        private var INSTANCE: CalcDatabase? = null

        fun getDatabase(context: Context): CalcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalcDatabase::class.java,
                    "infinity_calc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

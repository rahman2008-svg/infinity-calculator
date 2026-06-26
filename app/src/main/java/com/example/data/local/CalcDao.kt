package com.example.data.local

import androidx.room.*
import com.example.data.model.HistoryItem
import com.example.data.model.CalcNote
import kotlinx.coroutines.flow.Flow

@Dao
interface CalcDao {
    // History
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Update
    suspend fun updateHistory(item: HistoryItem)

    @Delete
    suspend fun deleteHistory(item: HistoryItem)

    @Query("DELETE FROM history_items WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM history_items")
    suspend fun clearAllHistory()

    // Notes
    @Query("SELECT * FROM calc_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<CalcNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CalcNote)

    @Update
    suspend fun updateNote(note: CalcNote)

    @Delete
    suspend fun deleteNote(note: CalcNote)

    @Query("DELETE FROM calc_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

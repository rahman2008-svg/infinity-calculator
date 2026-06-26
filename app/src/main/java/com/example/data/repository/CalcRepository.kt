package com.example.data.repository

import com.example.data.local.CalcDao
import com.example.data.model.HistoryItem
import com.example.data.model.CalcNote
import kotlinx.coroutines.flow.Flow

class CalcRepository(private val calcDao: CalcDao) {
    val allHistory: Flow<List<HistoryItem>> = calcDao.getAllHistory()
    val favoriteHistory: Flow<List<HistoryItem>> = calcDao.getFavoriteHistory()
    val allNotes: Flow<List<CalcNote>> = calcDao.getAllNotes()

    suspend fun insertHistory(item: HistoryItem) = calcDao.insertHistory(item)
    suspend fun updateHistory(item: HistoryItem) = calcDao.updateHistory(item)
    suspend fun deleteHistory(item: HistoryItem) = calcDao.deleteHistory(item)
    suspend fun deleteHistoryById(id: Int) = calcDao.deleteHistoryById(id)
    suspend fun clearAllHistory() = calcDao.clearAllHistory()

    suspend fun insertNote(note: CalcNote) = calcDao.insertNote(note)
    suspend fun updateNote(note: CalcNote) = calcDao.updateNote(note)
    suspend fun deleteNote(note: CalcNote) = calcDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = calcDao.deleteNoteById(id)
}

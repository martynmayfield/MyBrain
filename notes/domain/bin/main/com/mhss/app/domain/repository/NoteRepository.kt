package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    suspend fun getNote(id: Int): Note

    suspend fun searchNotes(query: String): List<Note>

    suspend fun addNote(note: Note): Long

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

}
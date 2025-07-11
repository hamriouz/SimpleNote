package com.example.simplenote

class NoteRepository(private val db: AppDatabase) {
    suspend fun insert(note: Note): Long = db.noteDao().insert(note)
    suspend fun update(note: Note) = db.noteDao().update(note)
    suspend fun getNoteById(id: Long): Note? = db.noteDao().getNoteById(id)
    suspend fun getAllNotes(): List<Note> = db.noteDao().getAllNotes()
} 
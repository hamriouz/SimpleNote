package com.example.simplenote

object NoteRepository {
    private val notes = mutableListOf<Note>()
    private var nextId = 1L

    fun getNotes(): List<Note> = notes

    fun addNote(title: String, content: String): Note {
        val note = Note(id = nextId++, title = title, content = content)
        notes.add(note)
        return note
    }

    fun updateNote(id: Long, title: String, content: String) {
        notes.find { it.id == id }?.apply {
            this.title = title
            this.content = content
            this.lastEdited = System.currentTimeMillis()
        }
    }

    fun deleteNote(id: Long) {
        notes.removeAll { it.id == id }
    }

    fun getNoteById(id: Long): Note? = notes.find { it.id == id }

    fun clear() {
        notes.clear()
        nextId = 1L
    }
} 
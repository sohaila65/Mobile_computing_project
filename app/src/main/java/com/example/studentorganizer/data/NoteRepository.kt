package com.example.studentorganizer.data

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insertNote(note: Note): Boolean {
        if (note.title.isBlank()) {
            return false
        }
        noteDao.insertNote(note)
        return true
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}

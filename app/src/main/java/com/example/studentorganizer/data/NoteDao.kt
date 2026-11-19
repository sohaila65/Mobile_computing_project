package com.example.studentorganizer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getNotesForCourse(courseId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE :query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE courseId = :courseId AND (:query = '' OR title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchNotesForCourse(courseId: Int, query: String): Flow<List<Note>>
}

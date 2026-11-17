package com.example.studentorganizer

import com.example.studentorganizer.data.Note
import com.example.studentorganizer.data.NoteDao
import com.example.studentorganizer.data.NoteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class NoteRepositoryTest {

    private lateinit var noteDao: NoteDao
    private lateinit var repository: NoteRepository

    @Before
    fun setup() {
        noteDao = mock()
        repository = NoteRepository(noteDao)
    }

    @Test
    fun insertNote_withEmptyTitle_returnsFalseAndDoesNotCallDao() {
        runBlocking {
            // Arrange
            val invalidNote = Note(
                courseId = 1,
                title = "",
                content = "Test content",
                tag = "Mobile Computing",
                createdAt = System.currentTimeMillis()
            )

            // Act
            val result = repository.insertNote(invalidNote)

            // Assert
            assertFalse(result)
            verifyNoInteractions(noteDao)
        }
    }

    @Test
    fun insertNote_withValidTitle_returnsTrueAndCallsDao() {
        runBlocking {
            // Arrange
            val validNote = Note(
                courseId = 1,
                title = "Study Guide",
                content = "Read chapter 1",
                tag = "Mobile Computing",
                createdAt = System.currentTimeMillis()
            )

            // Act
            val result = repository.insertNote(validNote)

            // Assert
            assertTrue(result)
            verify(noteDao).insertNote(validNote)
        }
    }

    @Test
    fun deleteNote_callsDeleteNoteOnDao() {
        runBlocking {
            // Arrange
            val noteToDelete = Note(
                noteId = 1,
                courseId = 1,
                title = "Old Note",
                content = "Outdated info",
                tag = "Mobile Computing",
                createdAt = System.currentTimeMillis()
            )

            // Act
            repository.deleteNote(noteToDelete)

            // Assert
            verify(noteDao).deleteNote(noteToDelete)
        }
    }
}

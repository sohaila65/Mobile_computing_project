package com.example.studentorganizer

import com.example.studentorganizer.data.SaveResult
import com.example.studentorganizer.data.TimetableDao
import com.example.studentorganizer.data.TimetableEntry
import com.example.studentorganizer.data.TimetableRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TimetableRepositoryTest {

    private lateinit var dao: TimetableDao
    private lateinit var repository: TimetableRepository

    @Before
    fun setup() {
        dao = mock()
        repository = TimetableRepository(dao)
    }

    private fun entry(id: Int = 0, start: Int, end: Int) =
        TimetableEntry(entryId = id, courseId = 1, dayOfWeek = 2, startMinutes = start, endMinutes = end)

    @Test
    fun save_invalidTimes_returnsInvalidAndDoesNotWrite() {
        runBlocking {
            val result = repository.save(entry(start = 600, end = 540))
            assertTrue(result is SaveResult.Invalid)
            verify(dao, never()).insertEntry(any())
            verify(dao, never()).updateEntry(any())
        }
    }

    @Test
    fun save_overlappingSession_returnsConflictAndDoesNotWrite() {
        runBlocking {
            val existing = entry(id = 3, start = 540, end = 600)
            whenever(dao.getEntriesForDayOnce(2)).thenReturn(listOf(existing))

            val result = repository.save(entry(start = 570, end = 630))

            assertEquals(SaveResult.Conflict(existing), result)
            verify(dao, never()).insertEntry(any())
        }
    }

    @Test
    fun save_newFreeSession_inserts() {
        runBlocking {
            whenever(dao.getEntriesForDayOnce(2)).thenReturn(emptyList())
            val newEntry = entry(start = 540, end = 600)

            assertEquals(SaveResult.Saved, repository.save(newEntry))
            verify(dao).insertEntry(newEntry)
        }
    }

    @Test
    fun save_existingSession_updates() {
        runBlocking {
            val edited = entry(id = 4, start = 540, end = 600)
            whenever(dao.getEntriesForDayOnce(2)).thenReturn(listOf(edited))

            assertEquals(SaveResult.Saved, repository.save(edited))
            verify(dao).updateEntry(edited)
            verify(dao, never()).insertEntry(any())
        }
    }
}

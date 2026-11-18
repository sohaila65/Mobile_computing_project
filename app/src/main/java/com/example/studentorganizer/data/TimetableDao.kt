package com.example.studentorganizer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Insert
    suspend fun insertEntry(entry: TimetableEntry)

    @Update
    suspend fun updateEntry(entry: TimetableEntry)

    @Delete
    suspend fun deleteEntry(entry: TimetableEntry)

    @Query("SELECT * FROM timetable_entries ORDER BY dayOfWeek, startMinutes")
    fun getAllEntries(): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable_entries WHERE dayOfWeek = :day ORDER BY startMinutes")
    suspend fun getEntriesForDayOnce(day: Int): List<TimetableEntry>

    /** Removes sessions whose course no longer exists in the courses database. */
    @Query("DELETE FROM timetable_entries WHERE courseId NOT IN (:validCourseIds)")
    suspend fun deleteEntriesNotIn(validCourseIds: List<Int>)
}

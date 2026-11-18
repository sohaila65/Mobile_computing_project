package com.example.studentorganizer.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One weekly class session in the timetable.
 *
 * Lives in its own database (see [TimetableDatabase]) so the existing
 * AppDatabase / Course tables are left untouched. [courseId] points at
 * Course.courseId in AppDatabase; the link is enforced in code
 * (see TimetableViewModel) because SQLite cannot hold a foreign key
 * across two database files.
 *
 * [dayOfWeek] uses java.util.Calendar constants (SUNDAY = 1 .. SATURDAY = 7).
 * [startMinutes] / [endMinutes] are minutes since midnight (0..1439).
 */
@Entity(
    tableName = "timetable_entries",
    indices = [Index(value = ["courseId"]), Index(value = ["dayOfWeek"])]
)
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val entryId: Int = 0,
    val courseId: Int,
    val dayOfWeek: Int,
    val startMinutes: Int,
    val endMinutes: Int,
    val location: String = ""
)

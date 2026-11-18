package com.example.studentorganizer.data

sealed class SaveResult {
    object Saved : SaveResult()
    data class Invalid(val problem: TimetableRules.Problem) : SaveResult()
    data class Conflict(val other: TimetableEntry) : SaveResult()
}

class TimetableRepository(private val dao: TimetableDao) {

    /** Inserts (entryId == 0) or updates a session after validating it and checking for clashes. */
    suspend fun save(entry: TimetableEntry): SaveResult {
        TimetableRules.validate(entry)?.let { return SaveResult.Invalid(it) }

        val sameDay = dao.getEntriesForDayOnce(entry.dayOfWeek)
        TimetableRules.findConflict(entry, sameDay)?.let { return SaveResult.Conflict(it) }

        if (entry.entryId == 0) dao.insertEntry(entry) else dao.updateEntry(entry)
        return SaveResult.Saved
    }

    suspend fun delete(entry: TimetableEntry) = dao.deleteEntry(entry)
}

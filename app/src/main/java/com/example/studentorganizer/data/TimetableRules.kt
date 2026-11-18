package com.example.studentorganizer.data

/** Pure validation helpers (no Android dependencies, easy to unit test). */
object TimetableRules {

    const val MINUTES_PER_DAY = 24 * 60

    enum class Problem { INVALID_DAY, INVALID_TIME, END_NOT_AFTER_START }

    fun validate(entry: TimetableEntry): Problem? = when {
        entry.dayOfWeek !in 1..7 -> Problem.INVALID_DAY
        entry.startMinutes !in 0 until MINUTES_PER_DAY ||
            entry.endMinutes !in 1..MINUTES_PER_DAY -> Problem.INVALID_TIME
        entry.endMinutes <= entry.startMinutes -> Problem.END_NOT_AFTER_START
        else -> null
    }

    /** Back-to-back sessions (one ends exactly when the next starts) do not overlap. */
    fun overlaps(a: TimetableEntry, b: TimetableEntry): Boolean =
        a.dayOfWeek == b.dayOfWeek &&
            a.startMinutes < b.endMinutes &&
            b.startMinutes < a.endMinutes

    /** First existing session that clashes with [candidate], ignoring the candidate itself. */
    fun findConflict(candidate: TimetableEntry, existing: List<TimetableEntry>): TimetableEntry? =
        existing.firstOrNull { it.entryId != candidate.entryId && overlaps(candidate, it) }
}

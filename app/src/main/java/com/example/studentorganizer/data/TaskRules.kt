package com.example.studentorganizer.data

import java.util.Calendar

/** Pure helpers (no Android dependencies) for validating and editing task dates. */
object TaskRules {

    const val HOUR_MS = 60L * 60L * 1000L

    enum class Problem { EMPTY_TITLE, END_NOT_AFTER_START }

    fun validate(task: Task): Problem? = when {
        task.title.isBlank() -> Problem.EMPTY_TITLE
        task.endMillis <= task.startMillis -> Problem.END_NOT_AFTER_START
        else -> null
    }

    /** [millis] with its calendar date replaced (month is 0-based like Calendar.MONTH). */
    fun withDate(millis: Long, year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(year, month, day)
        }.timeInMillis

    /** [millis] with its time of day replaced. */
    fun withTime(millis: Long, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun nextFullHour(nowMillis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.HOUR_OF_DAY, 1)
        }.timeInMillis

    fun sameDay(a: Long, b: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }
}

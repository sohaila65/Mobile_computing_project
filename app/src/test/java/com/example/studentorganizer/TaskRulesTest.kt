package com.example.studentorganizer

import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TaskRulesTest {

    private fun task(title: String = "T", start: Long = 1000, end: Long = 2000) =
        Task(title = title, startMillis = start, endMillis = end)

    @Test
    fun validate_acceptsNormalTask() = assertNull(TaskRules.validate(task()))

    @Test
    fun validate_rejectsBlankTitle() =
        assertEquals(TaskRules.Problem.EMPTY_TITLE, TaskRules.validate(task(title = "   ")))

    @Test
    fun validate_rejectsEndNotAfterStart() {
        assertEquals(TaskRules.Problem.END_NOT_AFTER_START, TaskRules.validate(task(start = 2000, end = 2000)))
        assertEquals(TaskRules.Problem.END_NOT_AFTER_START, TaskRules.validate(task(start = 2000, end = 1000)))
    }

    @Test
    fun withDateAndTime_changeOnlyTheirOwnPart() {
        val base = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 18, 9, 30, 0) }.timeInMillis
        val moved = TaskRules.withTime(TaskRules.withDate(base, 2026, Calendar.DECEMBER, 3), 14, 45)
        val c = Calendar.getInstance().apply { timeInMillis = moved }
        assertEquals(2026, c.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, c.get(Calendar.MONTH))
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, c.get(Calendar.MINUTE))
    }

    @Test
    fun sameDay_detectsDayBoundaries() {
        val a = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 18, 0, 5) }.timeInMillis
        val b = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 18, 23, 55) }.timeInMillis
        val c = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 19, 0, 5) }.timeInMillis
        assertTrue(TaskRules.sameDay(a, b))
        assertFalse(TaskRules.sameDay(b, c))
    }

    @Test
    fun nextFullHour_roundsUp() {
        val now = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 18, 9, 20, 30) }.timeInMillis
        val c = Calendar.getInstance().apply { timeInMillis = TaskRules.nextFullHour(now) }
        assertEquals(10, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, c.get(Calendar.MINUTE))
    }
}

package com.example.studentorganizer

import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.SampleTasks
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.data.TimetableEntry
import com.example.studentorganizer.ui.timetable.DayRow
import com.example.studentorganizer.ui.timetable.TimetableDays
import com.example.studentorganizer.ui.timetable.TimetableItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TimetableDaysTest {

    private fun millis(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0): Long =
        Calendar.getInstance().apply { set(y, m, d, h, min, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

    private val course = Course(courseId = 1, courseName = "Mobile", courseCode = "CS401", instructorName = "", creditHours = 3)

    // 2026-11-18 is a Wednesday; its week runs Sat Nov 14 .. Fri Nov 20.
    @Test
    fun weekStart_isAlwaysTheSaturdayOnOrBefore() {
        val saturday = millis(2026, Calendar.NOVEMBER, 14)
        for (day in 14..20) {
            val now = millis(2026, Calendar.NOVEMBER, day, 15, 30)
            assertEquals("day $day", saturday, TimetableDays.weekStartMillis(now, 0))
        }
        assertEquals(millis(2026, Calendar.NOVEMBER, 21), TimetableDays.weekStartMillis(millis(2026, Calendar.NOVEMBER, 21, 1), 0))
    }

    @Test
    fun weekOffset_movesBySevenDays() {
        val now = millis(2026, Calendar.NOVEMBER, 18, 12)
        assertEquals(millis(2026, Calendar.NOVEMBER, 21), TimetableDays.weekStartMillis(now, 1))
        assertEquals(millis(2026, Calendar.NOVEMBER, 7), TimetableDays.weekStartMillis(now, -1))
    }

    @Test
    fun dayOrder_startsOnSaturdayAndEndsOnFriday() {
        assertEquals(Calendar.SATURDAY, TimetableDays.DAY_ORDER.first())
        assertEquals(Calendar.FRIDAY, TimetableDays.DAY_ORDER.last())
        assertEquals(7, TimetableDays.DAY_ORDER.toSet().size)
        assertEquals(0, TimetableDays.todayIndex(millis(2026, Calendar.NOVEMBER, 14, 10)))
        assertEquals(6, TimetableDays.todayIndex(millis(2026, Calendar.NOVEMBER, 20, 10)))
    }

    @Test
    fun build_mergesSessionsAndTasksSortedByStart() {
        val wed = millis(2026, Calendar.NOVEMBER, 18)
        val session = TimetableItem(
            TimetableEntry(entryId = 1, courseId = 1, dayOfWeek = Calendar.WEDNESDAY, startMinutes = 600, endMinutes = 690), course
        )
        val otherDay = TimetableItem(
            TimetableEntry(entryId = 2, courseId = 1, dayOfWeek = Calendar.THURSDAY, startMinutes = 600, endMinutes = 690), course
        )
        val task = TaskItem(Task(taskId = 5, title = "T", startMillis = millis(2026, Calendar.NOVEMBER, 18, 9), endMillis = millis(2026, Calendar.NOVEMBER, 18, 10)), course)

        val rows = TimetableDays.build(wed, Calendar.WEDNESDAY, listOf(session, otherDay), listOf(task))

        assertEquals(listOf("t5", "s1"), rows.map { it.key })
    }

    @Test
    fun build_multiDayTaskShowsOnEveryDayItCovers() {
        val task = TaskItem(
            Task(taskId = 1, title = "Long", startMillis = millis(2026, Calendar.NOVEMBER, 16, 10), endMillis = millis(2026, Calendar.NOVEMBER, 18, 17)), null
        )
        fun row(day: Int) = TimetableDays.build(millis(2026, Calendar.NOVEMBER, day), 0, emptyList(), listOf(task)).singleOrNull() as DayRow.TaskEntry?

        assertEquals(null, row(15))
        val first = row(16)!!
        assertTrue(first.startsToday && !first.endsToday)
        assertEquals(600, first.startMinutes); assertEquals(1440, first.endMinutes)
        val middle = row(17)!!
        assertTrue(!middle.startsToday && !middle.endsToday)
        assertEquals(0, middle.startMinutes); assertEquals(1440, middle.endMinutes)
        val last = row(18)!!
        assertTrue(!last.startsToday && last.endsToday)
        assertEquals(0, last.startMinutes); assertEquals(17 * 60, last.endMinutes)
        assertEquals(null, row(19))
    }

    @Test
    fun build_taskEndingAtMidnightDoesNotLeakIntoNextDay() {
        val task = TaskItem(
            Task(taskId = 1, title = "Late", startMillis = millis(2026, Calendar.NOVEMBER, 18, 22), endMillis = millis(2026, Calendar.NOVEMBER, 19, 0)), null
        )
        assertEquals(1, TimetableDays.build(millis(2026, Calendar.NOVEMBER, 18), 0, emptyList(), listOf(task)).size)
        assertEquals(0, TimetableDays.build(millis(2026, Calendar.NOVEMBER, 19), 0, emptyList(), listOf(task)).size)
    }

    @Test
    fun sampleTasks_areValidLinkedAndInTheFuture() {
        val now = millis(2026, Calendar.NOVEMBER, 18, 12)
        val tasks = SampleTasks.build(now, mapOf("CS401" to 1, "CS301" to 3, "CS402" to 6))
        assertTrue(tasks.size >= 6)
        assertTrue(tasks.all { it.title.isNotBlank() && it.endMillis > it.startMillis })
        assertTrue(tasks.any { it.courseId == 1 })
        assertTrue(tasks.all { it.endMillis > now - 24 * 3600 * 1000L })
    }
}

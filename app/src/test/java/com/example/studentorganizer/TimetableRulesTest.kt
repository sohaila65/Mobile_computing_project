package com.example.studentorganizer

import com.example.studentorganizer.data.TimetableEntry
import com.example.studentorganizer.data.TimetableRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimetableRulesTest {

    private fun entry(day: Int = 2, start: Int, end: Int, id: Int = 0, course: Int = 1) =
        TimetableEntry(entryId = id, courseId = course, dayOfWeek = day, startMinutes = start, endMinutes = end)

    @Test
    fun validate_acceptsNormalSession() {
        assertNull(TimetableRules.validate(entry(start = 9 * 60, end = 10 * 60)))
    }

    @Test
    fun validate_acceptsSessionEndingAtMidnight() {
        assertNull(TimetableRules.validate(entry(start = 22 * 60, end = 24 * 60)))
    }

    @Test
    fun validate_rejectsEndBeforeOrEqualStart() {
        assertEquals(TimetableRules.Problem.END_NOT_AFTER_START, TimetableRules.validate(entry(start = 600, end = 600)))
        assertEquals(TimetableRules.Problem.END_NOT_AFTER_START, TimetableRules.validate(entry(start = 600, end = 540)))
    }

    @Test
    fun validate_rejectsOutOfRangeValues() {
        assertEquals(TimetableRules.Problem.INVALID_DAY, TimetableRules.validate(entry(day = 0, start = 60, end = 120)))
        assertEquals(TimetableRules.Problem.INVALID_DAY, TimetableRules.validate(entry(day = 8, start = 60, end = 120)))
        assertEquals(TimetableRules.Problem.INVALID_TIME, TimetableRules.validate(entry(start = -1, end = 120)))
        assertEquals(TimetableRules.Problem.INVALID_TIME, TimetableRules.validate(entry(start = 60, end = 1441)))
    }

    @Test
    fun overlaps_detectsPartialAndContainedOverlap() {
        assertTrue(TimetableRules.overlaps(entry(start = 540, end = 630), entry(start = 600, end = 690)))
        assertTrue(TimetableRules.overlaps(entry(start = 540, end = 720), entry(start = 600, end = 660)))
    }

    @Test
    fun overlaps_backToBackSessionsDoNotOverlap() {
        assertFalse(TimetableRules.overlaps(entry(start = 540, end = 600), entry(start = 600, end = 660)))
    }

    @Test
    fun overlaps_differentDaysDoNotOverlap() {
        assertFalse(TimetableRules.overlaps(entry(day = 2, start = 540, end = 600), entry(day = 3, start = 540, end = 600)))
    }

    @Test
    fun findConflict_ignoresTheEntryBeingEdited() {
        val existing = listOf(entry(id = 5, start = 540, end = 600))
        assertNull(TimetableRules.findConflict(entry(id = 5, start = 550, end = 610), existing))
    }

    @Test
    fun findConflict_returnsTheClashingEntry() {
        val clash = entry(id = 7, start = 600, end = 660)
        val existing = listOf(entry(id = 6, start = 480, end = 540), clash)
        assertEquals(clash, TimetableRules.findConflict(entry(start = 630, end = 700), existing))
    }
}

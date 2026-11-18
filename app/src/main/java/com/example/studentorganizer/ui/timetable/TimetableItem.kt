package com.example.studentorganizer.ui.timetable

import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.TimetableEntry

/** A timetable session joined with the course it belongs to. */
data class TimetableItem(val entry: TimetableEntry, val course: Course)

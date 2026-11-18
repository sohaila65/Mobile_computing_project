package com.example.studentorganizer.data

import java.util.Calendar

/** Dummy project-related tasks, scheduled relative to "now" so they land in the coming days. */
object SampleTasks {

    private class Spec(
        val title: String,
        val description: String,
        val courseCode: String,
        val startDay: Int, val startHour: Int,
        val endDay: Int, val endHour: Int, val endMinute: Int = 0
    )

    private val specs = listOf(
        Spec("Project proposal & requirements", "Write the problem statement, user stories and scope of the Student Organizer app.", "CS402", 0, 14, 0, 16),
        Spec("Design UI wireframes", "Sketch Home, Tasks, Courses, Timetable and Notes screens and agree on the navigation.", "CS401", 1, 10, 2, 18),
        Spec("ER diagram for the project database", "Model courses, notes, tasks and timetable sessions and their relations.", "CS301", 2, 11, 2, 13, 30),
        Spec("Implement Room database layer", "Entities, DAOs and repositories for courses, notes, tasks and timetable.", "CS401", 3, 9, 5, 17),
        Spec("Build Tasks and Timetable screens", "RecyclerView lists, add/edit/delete dialogs and the weekly timetable view.", "CS401", 4, 12, 6, 20),
        Spec("Write the project test plan", "Unit tests for the repositories and manual test cases for each screen.", "CS402", 6, 10, 7, 15),
        Spec("Prepare project presentation slides", "Architecture overview, screenshots and a short live demo script.", "CS401", 8, 9, 9, 12),
        Spec("Final demo & report submission", "Present the app to the instructor and upload the final report and source code.", "CS401", 10, 10, 10, 14)
    )

    fun build(nowMillis: Long, courseIdByCode: Map<String, Int>): List<Task> {
        fun at(day: Int, hour: Int, minute: Int): Long =
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }.timeInMillis

        return specs.map { s ->
            Task(
                title = s.title,
                description = s.description,
                courseId = courseIdByCode[s.courseCode],
                startMillis = at(s.startDay, s.startHour, 0),
                endMillis = at(s.endDay, s.endHour, s.endMinute)
            )
        }
    }
}

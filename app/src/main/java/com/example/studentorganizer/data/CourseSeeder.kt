package com.example.studentorganizer.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Adds a few starter courses the first time the app runs.
 *
 * - Runs only once (flag stored in SharedPreferences), so courses the user
 *   deletes are never re-added.
 * - Skipped if the user already has courses.
 * - Uses the normal CourseDao, so users can keep adding/deleting courses as usual.
 */
object CourseSeeder {

    private const val PREFS = "course_seeder"
    private const val KEY_SEEDED = "starter_courses_seeded"

    private val starterCourses = listOf(
        Course(courseName = "Mobile Computing", courseCode = "CS401", instructorName = "Dr. Ahmed Hassan", creditHours = 3),
        Course(courseName = "Data Structures", courseCode = "CS201", instructorName = "Dr. Mona Khaled", creditHours = 3),
        Course(courseName = "Database Systems", courseCode = "CS301", instructorName = "Dr. Omar Salem", creditHours = 3),
        Course(courseName = "Operating Systems", courseCode = "CS303", instructorName = "Dr. Laila Mostafa", creditHours = 3),
        Course(courseName = "Computer Networks", courseCode = "CS305", instructorName = "Dr. Youssef Adel", creditHours = 3),
        Course(courseName = "Software Engineering", courseCode = "CS402", instructorName = "Dr. Hana Ibrahim", creditHours = 3)
    )

    /** Returns the seeding job, or null when seeding was already done. */
    fun seedIfNeeded(context: Context): Job? {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return null

        return CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(appContext).courseDao()
            if (dao.getAllCourses().first().isEmpty()) {
                starterCourses.forEach { dao.insertCourse(it) }
            }
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
    }
}

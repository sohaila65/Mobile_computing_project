package com.example.studentorganizer.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Adds dummy project tasks the first time the app runs (once only; deleted tasks never come back).
 * Waits for [after] (the course seeder) so the tasks can be linked to the starter courses.
 */
object TaskSeeder {

    private const val PREFS = "task_seeder"
    private const val KEY_SEEDED = "starter_tasks_seeded"

    fun seedIfNeeded(context: Context, after: Job? = null) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            after?.join()
            val taskDao = TaskDatabase.getInstance(appContext).taskDao()
            if (taskDao.count() == 0) {
                val courses = AppDatabase.getInstance(appContext).courseDao().getAllCourses().first()
                val idByCode = courses.associate { it.courseCode to it.courseId }
                SampleTasks.build(System.currentTimeMillis(), idByCode).forEach { taskDao.insertTask(it) }
            }
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
        }
    }
}

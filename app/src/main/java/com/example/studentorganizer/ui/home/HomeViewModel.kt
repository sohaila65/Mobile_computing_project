package com.example.studentorganizer.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskDatabase
import com.example.studentorganizer.data.TimetableDatabase
import com.example.studentorganizer.ui.timetable.TimetableItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

data class HomeState(
    val openTaskCount: Int,
    val courseCount: Int,
    val noteCount: Int,
    val todayTasks: List<Task>,
    val nextClass: NextClass?
)


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    //database
    private val appDatabase = AppDatabase.getInstance(application)
    private val taskDao = TaskDatabase.getInstance(application).taskDao()
    private val timetableDao = TimetableDatabase.getInstance(application).timetableDao()
    private val clock = MutableStateFlow(System.currentTimeMillis())


    //live data
    val state: LiveData<HomeState> = combine(
        appDatabase.courseDao().getAllCourses(),
        appDatabase.noteDao().getAllNotes(),
        taskDao.getAllTasks(),
        timetableDao.getAllEntries(),
        clock
    ) { courses, notes, tasks, entries, now ->
        val courseIds = courses.map { it.courseId }.toSet()
        val coursesById = courses.associateBy { it.courseId }
        val visibleTasks = tasks.filter { task ->
            task.courseId.let { id -> id == null || id in courseIds }
        }
        val sessions = entries.mapNotNull { entry ->
            coursesById[entry.courseId]?.let { TimetableItem(entry, it) }
        }
        HomeState(
            openTaskCount = HomeRules.openTasks(visibleTasks).size,
            courseCount = courses.size,
            noteCount = notes.size,
            todayTasks = HomeRules.tasksDueToday(visibleTasks, now),
            nextClass = HomeRules.nextClass(sessions, now)
        )
    }.asLiveData()
    fun refresh() {
        clock.value = System.currentTimeMillis()
    }
}

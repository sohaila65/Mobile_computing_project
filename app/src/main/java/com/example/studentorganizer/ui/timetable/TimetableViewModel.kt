package com.example.studentorganizer.ui.timetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.SaveResult
import com.example.studentorganizer.data.TimetableDatabase
import com.example.studentorganizer.data.TimetableEntry
import com.example.studentorganizer.data.TimetableRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    // Courses come from the existing courses implementation (read-only use).
    private val courseDao = AppDatabase.getInstance(application).courseDao()
    private val timetableDao = TimetableDatabase.getInstance(application).timetableDao()
    private val repository = TimetableRepository(timetableDao)

    val courses: LiveData<List<Course>> = courseDao.getAllCourses()
        .map { list -> list.sortedBy { it.courseName.lowercase() } }
        .asLiveData()

    /** Sessions of courses that still exist, ordered by day then start time. */
    val items: LiveData<List<TimetableItem>> =
        combine(courseDao.getAllCourses(), timetableDao.getAllEntries()) { courses, entries ->
            val byId = courses.associateBy { it.courseId }
            entries.mapNotNull { e -> byId[e.courseId]?.let { TimetableItem(e, it) } }
        }.asLiveData()

    init {
        // A deleted course must not leave sessions behind in the timetable database.
        viewModelScope.launch {
            courseDao.getAllCourses().collect { courses ->
                timetableDao.deleteEntriesNotIn(courses.map { it.courseId })
            }
        }
    }

    fun save(entry: TimetableEntry, onResult: (SaveResult) -> Unit) {
        viewModelScope.launch { onResult(repository.save(entry)) }
    }

    fun delete(entry: TimetableEntry) {
        viewModelScope.launch { repository.delete(entry) }
    }
}

package com.example.studentorganizer.ui.courses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.CourseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val courseDao: CourseDao = AppDatabase.getInstance(application).courseDao()
    private val searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val allCourses: LiveData<List<Course>> = searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                courseDao.getAllCourses()
            } else {
                courseDao.searchCourses(trimmed)
            }
        }
        .asLiveData()

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun insert(course: Course) = viewModelScope.launch {
        courseDao.insertCourse(course)
    }

    fun update(course: Course) = viewModelScope.launch {
        courseDao.updateCourse(course)
    }

    fun delete(course: Course) = viewModelScope.launch {
        courseDao.deleteCourse(course)
    }
}

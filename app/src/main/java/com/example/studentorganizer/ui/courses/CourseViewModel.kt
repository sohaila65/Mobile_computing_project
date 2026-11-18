package com.example.studentorganizer.ui.courses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.CourseDao
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val courseDao: CourseDao = AppDatabase.getInstance(application).courseDao()

    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses().asLiveData()

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

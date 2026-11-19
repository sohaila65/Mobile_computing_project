package com.example.studentorganizer.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskDatabase
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.data.TaskRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    // Courses come from the existing courses implementation (read-only use).
    private val courseDao = AppDatabase.getInstance(application).courseDao()
    private val taskDao = TaskDatabase.getInstance(application).taskDao()
    private val repository = TaskRepository(taskDao)

    private val searchQuery = MutableStateFlow("")

    val courses: LiveData<List<Course>> = courseDao.getAllCourses()
        .map { list -> list.sortedBy { it.courseName.lowercase() } }
        .asLiveData()

    /** Tasks joined with their course; unfinished first, then by start time. */
    @OptIn(FlowPreview::class)
    val items: LiveData<List<TaskItem>> =
        combine(
            courseDao.getAllCourses(),
            taskDao.getAllTasks(),
            searchQuery.debounce(300L)
        ) { courses, tasks, query ->
            val byId = courses.associateBy { it.courseId }
            val trimmedQuery = query.trim()

            val allItems = tasks.mapNotNull { task ->
                val courseId = task.courseId
                when {
                    courseId == null -> TaskItem(task, null)
                    else -> byId[courseId]?.let { TaskItem(task, it) } // course deleted -> hide
                }
            }

            if (trimmedQuery.isEmpty()) {
                allItems
            } else {
                allItems.filter { item ->
                    matchesTaskItem(item, trimmedQuery)
                }
            }
        }.asLiveData()

    init {
        // A deleted course must not leave tasks behind in the task database.
        viewModelScope.launch {
            courseDao.getAllCourses().collect { courses ->
                taskDao.deleteTasksNotIn(courses.map { it.courseId })
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun save(task: Task) = viewModelScope.launch { repository.save(task) }

    fun delete(task: Task) = viewModelScope.launch { repository.delete(task) }

    fun setCompleted(task: Task, completed: Boolean) =
        viewModelScope.launch { repository.setCompleted(task, completed) }

    private fun matchesTaskItem(item: TaskItem, query: String): Boolean {
        val q = query.lowercase()
        val task = item.task
        val course = item.course

        if (task.title.lowercase().contains(q)) return true
        if (task.description.lowercase().contains(q)) return true

        if (course != null) {
            if (course.courseName.lowercase().contains(q)) return true
            if (course.courseCode.lowercase().contains(q)) return true
            if (course.instructorName.lowercase().contains(q)) return true
        }

        val now = System.currentTimeMillis()
        val statusText = when {
            task.isCompleted -> "completed done"
            task.endMillis < now -> "overdue pending"
            else -> "pending"
        }
        if (statusText.contains(q)) return true

        return false
    }
}

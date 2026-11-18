package com.example.studentorganizer.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A task / project work item with a start and an end date-time (epoch millis).
 *
 * Lives in TaskDatabase (separate from AppDatabase, which is left untouched).
 * [courseId] optionally points at Course.courseId; null means "no course".
 */
@Entity(tableName = "tasks", indices = [Index(value = ["courseId"])])
data class Task(
    @PrimaryKey(autoGenerate = true) val taskId: Int = 0,
    val title: String,
    val description: String = "",
    val courseId: Int? = null,
    val startMillis: Long,
    val endMillis: Long,
    val isCompleted: Boolean = false
)

/** A task joined with its course (null when the task has no course). */
data class TaskItem(val task: Task, val course: Course?)

package com.example.studentorganizer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY isCompleted, startMillis")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    /** Removes tasks linked to a course that no longer exists (tasks without a course are kept). */
    @Query("DELETE FROM tasks WHERE courseId IS NOT NULL AND courseId NOT IN (:validCourseIds)")
    suspend fun deleteTasksNotIn(validCourseIds: List<Int>)
}

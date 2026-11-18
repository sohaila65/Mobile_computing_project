package com.example.studentorganizer

import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskDao
import com.example.studentorganizer.data.TaskRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class TaskRepositoryTest {

    private lateinit var dao: TaskDao
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        dao = mock()
        repository = TaskRepository(dao)
    }

    @Test
    fun save_newTask_inserts() = runBlocking {
        val task = Task(title = "Wireframes", startMillis = 1000, endMillis = 2000)
        assertTrue(repository.save(task))
        verify(dao).insertTask(task)
        verify(dao, never()).updateTask(any())
    }

    @Test
    fun save_existingTask_updates() = runBlocking {
        val task = Task(taskId = 4, title = "Wireframes", startMillis = 1000, endMillis = 2000)
        assertTrue(repository.save(task))
        verify(dao).updateTask(task)
        verify(dao, never()).insertTask(any())
    }

    @Test
    fun save_invalidTask_writesNothing() = runBlocking {
        val task = Task(title = "", startMillis = 1000, endMillis = 2000)
        assertFalse(repository.save(task))
        verify(dao, never()).insertTask(any())
        verify(dao, never()).updateTask(any())
    }

    @Test
    fun delete_removesTask() = runBlocking {
        val task = Task(taskId = 2, title = "X", startMillis = 1, endMillis = 2)
        repository.delete(task)
        verify(dao).deleteTask(task)
    }

    @Test
    fun setCompleted_updatesFlag() = runBlocking {
        val task = Task(taskId = 2, title = "X", startMillis = 1, endMillis = 2)
        repository.setCompleted(task, true)
        verify(dao).updateTask(task.copy(isCompleted = true))
    }
}

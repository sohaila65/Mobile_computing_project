package com.example.studentorganizer.data

class TaskRepository(private val dao: TaskDao) {

    /** Inserts (taskId == 0) or updates a task. Returns false, writing nothing, if it is invalid. */
    suspend fun save(task: Task): Boolean {
        if (TaskRules.validate(task) != null) return false
        if (task.taskId == 0) dao.insertTask(task) else dao.updateTask(task)
        return true
    }

    suspend fun delete(task: Task) = dao.deleteTask(task)

    suspend fun setCompleted(task: Task, completed: Boolean) =
        dao.updateTask(task.copy(isCompleted = completed))
}

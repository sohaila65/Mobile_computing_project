package ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.Task

import data.AppDatabase 

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    
    
     private val taskDao = AppDatabase.getDatabase(application).taskDao()
    val allTasks = taskDao.getAllTasks().asLiveData()

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
             taskDao.deleteTask(task)
        }
    }
    
    fun addTask(task: Task) {
        viewModelScope.launch {
             taskDao.insertTask(task)
        }
    }
}
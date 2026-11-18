package data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getInstance(application).taskDao()
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

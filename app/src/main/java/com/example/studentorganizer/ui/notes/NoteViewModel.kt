package com.example.studentorganizer.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Note
import com.example.studentorganizer.data.NoteDao
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao: NoteDao = AppDatabase.getInstance(application).noteDao()

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes().asLiveData()

    fun getNotesForCourse(courseId: Int): LiveData<List<Note>> {
        return noteDao.getNotesForCourse(courseId).asLiveData()
    }

    fun insert(note: Note) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote(note)
    }
}

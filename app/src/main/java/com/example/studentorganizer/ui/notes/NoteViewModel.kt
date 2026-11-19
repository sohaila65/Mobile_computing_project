package com.example.studentorganizer.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentorganizer.data.AppDatabase
import com.example.studentorganizer.data.Note
import com.example.studentorganizer.data.NoteDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao: NoteDao = AppDatabase.getInstance(application).noteDao()
    private val searchQuery = MutableStateFlow("")
    private val currentCourseId = MutableStateFlow<Int?>(null)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val allNotes: LiveData<List<Note>> = combine(
        searchQuery.debounce(300L),
        currentCourseId
    ) { query, courseId ->
        Pair(query.trim(), courseId)
    }.flatMapLatest { (query, courseId) ->
        if (courseId != null && courseId != -1) {
            noteDao.searchNotesForCourse(courseId, query)
        } else {
            noteDao.searchNotes(query)
        }
    }.asLiveData()

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCourseId(courseId: Int?) {
        currentCourseId.value = courseId
    }

    fun getNotesForCourse(courseId: Int): LiveData<List<Note>> {
        setCourseId(courseId)
        return allNotes
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

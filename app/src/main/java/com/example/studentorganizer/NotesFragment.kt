package com.example.studentorganizer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.data.Note
import com.example.studentorganizer.ui.notes.NoteViewModel
import com.example.studentorganizer.ui.notes.NotesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class NotesFragment : Fragment(R.layout.fragment_notes) {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotes)
        val fabAddNote = view.findViewById<FloatingActionButton>(R.id.fabAddNote)
        val etSearchNotes = view.findViewById<TextInputEditText>(R.id.etSearchNotes)
        val tvNotesEmpty = view.findViewById<TextView>(R.id.tvNotesEmpty)

        adapter = NotesAdapter(
            onShareClick = { note ->
                shareNote(note)
            },
            onNoteLongClick = { note ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete '${note.title}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.delete(note)
                        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val courseId = arguments?.getInt("courseId", -1) ?: -1
        val courseName = arguments?.getString("courseName") ?: ""

        if (courseId != -1) {
            viewModel.setCourseId(courseId)
        } else {
            viewModel.setCourseId(null)
        }

        etSearchNotes.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        viewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            val query = etSearchNotes.text?.toString()?.trim() ?: ""
            tvNotesEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            tvNotesEmpty.text = if (query.isNotEmpty()) {
                getString(R.string.notes_search_empty)
            } else {
                getString(R.string.notes_empty)
            }
        }

        fabAddNote.setOnClickListener {
            if (courseId == -1) {
                Toast.makeText(
                    requireContext(),
                    "Please select a course from the Courses tab to add a note.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showAddNoteDialog(courseId, courseName)
            }
        }
    }

    private fun shareNote(note: Note) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            
            val tagString = if (note.tag.isNullOrBlank()) "" else "#${note.tag.removePrefix("#")}\n"
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n$tagString\n${note.content}")
        }
        startActivity(Intent.createChooser(intent, "Share Note"))
    }

    private fun showAddNoteDialog(courseId: Int, courseName: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextNoteTitle)
        val contentEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextNoteContent)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val content = contentEditText.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val newNote = Note(
                        courseId = courseId,
                        title = title,
                        content = content,
                        tag = courseName,
                        createdAt = System.currentTimeMillis()
                    )
                    viewModel.insert(newNote)
                } else {
                    Toast.makeText(requireContext(), "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

package com.example.studentorganizer.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.R
import com.example.studentorganizer.data.Note

class NotesAdapter(
    private val onShareClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view, onShareClick, onNoteLongClick)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    class NoteViewHolder(
        itemView: View,
        private val onShareClick: (Note) -> Unit,
        private val onNoteLongClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewNoteTitle)
        private val tagTextView: TextView = itemView.findViewById(R.id.textViewNoteTag)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewNoteContent)
        private val shareButton: ImageButton = itemView.findViewById(R.id.buttonShare)

        fun bind(note: Note) {
            titleTextView.text = note.title
            
            if (note.tag.isNullOrBlank()) {
                tagTextView.visibility = View.GONE
            } else {
                tagTextView.visibility = View.VISIBLE
                tagTextView.text = if (note.tag.startsWith("#")) note.tag else "#${note.tag}"
            }
            
            contentTextView.text = note.content
            shareButton.setOnClickListener {
                onShareClick(note)
            }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}

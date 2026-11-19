package com.example.studentorganizer.ui.notes

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.data.Note
import com.example.studentorganizer.databinding.ItemNoteBinding
import com.example.studentorganizer.ui.timetable.TimetableFormat

class NotesAdapter(
    private val onShareClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    /** Id of the currently expanded note, or -1 when none is expanded. */
    private var expandedNoteId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            // ── Colour accent bar (use courseId to pick a palette colour) ───
            binding.viewNoteBar.setBackgroundColor(
                TimetableFormat.colorFor(note.courseId)
            )

            // ── Collapsed section ───────────────────────────────────────────
            binding.textViewNoteTitle.text = note.title

            if (note.tag.isBlank()) {
                binding.textViewNoteTag.visibility = View.GONE
            } else {
                val tag = if (note.tag.startsWith("#")) note.tag else "#${note.tag}"
                binding.textViewNoteTag.text = tag
                binding.textViewNoteTag.visibility = View.VISIBLE
            }

            // Brief preview in collapsed state (2 lines via XML maxLines)
            binding.textViewNoteContent.text = note.content

            // ── Expanded section ────────────────────────────────────────────
            binding.tvNoteExpandedTitle.text = note.title
            binding.tvNoteExpandedContent.text = note.content

            val tagForExpanded = when {
                note.tag.isBlank() -> ""
                note.tag.startsWith("#") -> note.tag.removePrefix("#")
                else -> note.tag
            }
            binding.tvNoteExpandedTag.text = tagForExpanded.ifBlank { "—" }

            // ── Action buttons (inside expanded panel) ──────────────────────
            binding.btnShareNote.setOnClickListener { onShareClick(note) }
            binding.btnDeleteNote.setOnClickListener { onNoteLongClick(note) }

            // ── Expand / collapse state (instant on rebind to avoid flicker) ─
            val isExpanded = note.noteId == expandedNoteId
            setExpandedInstant(isExpanded)
            binding.ivNoteChevron.rotation = if (isExpanded) 270f else 90f

            // ── Card click → toggle expand ──────────────────────────────────
            binding.cardNote.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener

                val previousId = expandedNoteId
                val clickedId = note.noteId

                if (previousId == clickedId) {
                    expandedNoteId = -1
                    animateExpand(false)
                } else {
                    if (previousId != -1) {
                        val prevPos = currentList.indexOfFirst { it.noteId == previousId }
                        if (prevPos != -1) notifyItemChanged(prevPos)
                    }
                    expandedNoteId = clickedId
                    animateExpand(true)
                }
            }

            // Long-press → delete confirm (original behaviour)
            binding.cardNote.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private fun setExpandedInstant(expanded: Boolean) {
            binding.layoutNoteExpanded.visibility = if (expanded) View.VISIBLE else View.GONE
            binding.dividerNoteExpand.visibility = if (expanded) View.VISIBLE else View.GONE
        }

        private fun animateExpand(expand: Boolean) {
            val panel = binding.layoutNoteExpanded
            val divider = binding.dividerNoteExpand
            val chevron = binding.ivNoteChevron

            panel.measure(
                View.MeasureSpec.makeMeasureSpec(panel.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val targetH = panel.measuredHeight

            if (expand) {
                divider.visibility = View.VISIBLE
                panel.visibility = View.VISIBLE
                panel.layoutParams.height = 0
                panel.requestLayout()

                ValueAnimator.ofInt(0, targetH).apply {
                    duration = 220
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { anim ->
                        panel.layoutParams.height = anim.animatedValue as Int
                        panel.requestLayout()
                    }
                    start()
                }
            } else {
                val startH = panel.height
                ValueAnimator.ofInt(startH, 0).apply {
                    duration = 180
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { anim ->
                        panel.layoutParams.height = anim.animatedValue as Int
                        panel.requestLayout()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            panel.visibility = View.GONE
                            panel.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            divider.visibility = View.GONE
                        }
                    })
                    start()
                }
            }

            chevron.animate().rotation(if (expand) 270f else 90f).setDuration(200).start()
        }
    }

    // ─── DiffUtil ──────────────────────────────────────────────────────────────

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.noteId == newItem.noteId
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}

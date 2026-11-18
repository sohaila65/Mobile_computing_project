package com.example.studentorganizer.ui.timetable

import android.content.Context
import android.graphics.Paint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.R
import com.example.studentorganizer.databinding.ItemTimetableEntryBinding
import java.util.Calendar

object TimetableFormat {
    private val palette = intArrayOf(
        0xFF1E88E5.toInt(), 0xFF43A047.toInt(), 0xFFF4511E.toInt(),
        0xFF8E24AA.toInt(), 0xFF00897B.toInt(), 0xFFFB8C00.toInt(),
        0xFFD81B60.toInt(), 0xFF3949AB.toInt()
    )

    /** Colour used for tasks that are not linked to a course. */
    val NO_COURSE_COLOR: Int = 0xFF757575.toInt()

    fun colorFor(courseId: Int): Int = palette[Math.floorMod(courseId, palette.size)]

    fun time(context: Context, minutes: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
        }
        return DateFormat.getTimeFormat(context).format(cal.time)
    }

    fun range(context: Context, start: Int, end: Int): String =
        "${time(context, start)} – ${time(context, end)}"
}

/** Shows class sessions and dated tasks of one day in a single list. */
class TimetableAdapter(
    private val onItemClick: (DayRow) -> Unit,
    private val onItemLongClick: (DayRow) -> Unit
) : ListAdapter<DayRow, TimetableAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimetableEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTimetableEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun strike(view: TextView, on: Boolean) {
            view.paintFlags = if (on) {
                view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        fun bind(row: DayRow) {
            val ctx = binding.root.context
            when (row) {
                is DayRow.Session -> {
                    val entry = row.item.entry
                    val course = row.item.course
                    binding.viewColorBar.setBackgroundColor(TimetableFormat.colorFor(course.courseId))
                    binding.tvTime.text = TimetableFormat.range(ctx, entry.startMinutes, entry.endMinutes)
                    binding.tvCourse.text = course.courseName
                    strike(binding.tvCourse, false)
                    binding.tvDetails.text = listOf(course.courseCode, entry.location)
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                }
                is DayRow.TaskEntry -> {
                    val task = row.item.task
                    val course = row.item.course
                    binding.viewColorBar.setBackgroundColor(
                        course?.let { TimetableFormat.colorFor(it.courseId) } ?: TimetableFormat.NO_COURSE_COLOR
                    )
                    val label = when {
                        row.startsToday && row.endsToday ->
                            TimetableFormat.range(ctx, row.startMinutes, row.endMinutes)
                        row.startsToday ->
                            ctx.getString(R.string.timetable_from, TimetableFormat.time(ctx, row.startMinutes))
                        row.endsToday ->
                            ctx.getString(R.string.timetable_until, TimetableFormat.time(ctx, row.endMinutes))
                        else -> ctx.getString(R.string.timetable_all_day)
                    }
                    binding.tvTime.text = ctx.getString(R.string.timetable_task_time, label)
                    binding.tvCourse.text = task.title
                    strike(binding.tvCourse, task.isCompleted)
                    binding.tvDetails.text = course?.let {
                        if (it.courseCode.isBlank()) it.courseName else "${it.courseName} (${it.courseCode})"
                    }.orEmpty()
                }
            }
            binding.tvDetails.visibility =
                if (binding.tvDetails.text.isNullOrBlank()) View.GONE else View.VISIBLE

            binding.root.setOnClickListener { onItemClick(row) }
            binding.root.setOnLongClickListener {
                onItemLongClick(row)
                true
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<DayRow>() {
        override fun areItemsTheSame(a: DayRow, b: DayRow) = a.key == b.key
        override fun areContentsTheSame(a: DayRow, b: DayRow) = a == b
    }
}

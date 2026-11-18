package com.example.studentorganizer.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.databinding.ItemTaskBinding
import com.example.studentorganizer.ui.timetable.TimetableFormat

class TasksAdapter(
    private val onToggle: (Task, Boolean) -> Unit,
    private val onItemClick: (TaskItem) -> Unit,
    private val onItemLongClick: (TaskItem) -> Unit
) : ListAdapter<TaskItem, TasksAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskItem) {
            val ctx = binding.root.context
            val task = item.task
            val course = item.course

            binding.viewTaskBar.setBackgroundColor(
                course?.let { TimetableFormat.colorFor(it.courseId) } ?: TimetableFormat.NO_COURSE_COLOR
            )
            binding.tvTaskTitle.text = task.title
            binding.tvTaskTitle.paintFlags = if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.tvTaskCourse.text = course?.let {
                if (it.courseCode.isBlank()) it.courseName else "${it.courseName} (${it.courseCode})"
            }
            binding.tvTaskCourse.visibility = if (course == null) View.GONE else View.VISIBLE

            binding.tvTaskDates.text = TaskFormat.range(ctx, task.startMillis, task.endMillis)

            binding.tvTaskDescription.text = task.description
            binding.tvTaskDescription.visibility =
                if (task.description.isBlank()) View.GONE else View.VISIBLE

            // Detach the listener before setting the state so recycling does not fire it.
            binding.cbTaskDone.setOnCheckedChangeListener(null)
            binding.cbTaskDone.isChecked = task.isCompleted
            binding.cbTaskDone.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }

            binding.root.setOnClickListener { onItemClick(item) }
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(a: TaskItem, b: TaskItem) = a.task.taskId == b.task.taskId
        override fun areContentsTheSame(a: TaskItem, b: TaskItem) = a == b
    }
}

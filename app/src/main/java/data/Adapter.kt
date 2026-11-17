package ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import data.Task
import com.example.mobile_computing_project.databinding.ItemTaskBinding 

class TasksAdapter(
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskType.text = task.type
            binding.cbTaskCompleted.isChecked = task.isCompleted

            binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (task.isCompleted != isChecked) {
                    onTaskCheckedChange(task, isChecked)
                }
            }

            binding.btnDeleteTask.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): taskId == newItem.taskId
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
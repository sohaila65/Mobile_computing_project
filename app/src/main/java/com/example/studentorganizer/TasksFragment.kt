package com.example.studentorganizer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.data.TaskRules
import com.example.studentorganizer.databinding.FragmentTasksBinding
import com.example.studentorganizer.ui.tasks.TaskDialog
import com.example.studentorganizer.ui.tasks.TaskViewModel
import com.example.studentorganizer.ui.tasks.TasksAdapter

class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var tasksAdapter: TasksAdapter
    private var latestCourses: List<Course> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTasksBinding.bind(view)

        tasksAdapter = TasksAdapter(
            onToggle = { task, done -> viewModel.setCompleted(task, done) },
            onItemClick = { item -> showTaskDialog(item.task) },
            onItemLongClick = { item -> confirmDelete(item) }
        )
        binding.rvTasks.adapter = tasksAdapter

        viewModel.courses.observe(viewLifecycleOwner) { latestCourses = it }
        viewModel.items.observe(viewLifecycleOwner) { items ->
            tasksAdapter.submitList(items)
            binding.tvTasksEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddTask.setOnClickListener { showTaskDialog(null) }
    }

    private fun showTaskDialog(existing: Task?) {
        val ctx = requireContext()
        TaskDialog.show(ctx, latestCourses, existing, TaskRules.nextFullHour(System.currentTimeMillis())) { task ->
            viewModel.save(task)
            // Tasks share the timetable's data, so a saved task shows up there immediately.
            if (existing == null) Toast.makeText(ctx, R.string.tasks_added, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDelete(item: TaskItem) {
        val ctx = requireContext()
        AlertDialog.Builder(ctx)
            .setTitle(R.string.tasks_delete_title)
            .setMessage(getString(R.string.tasks_delete_message, item.task.title))
            .setPositiveButton(R.string.tasks_delete) { _, _ ->
                viewModel.delete(item.task)
                Toast.makeText(ctx, R.string.tasks_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.tasks_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

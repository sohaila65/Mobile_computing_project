package com.example.studentorganizer.ui.tasks

import android.animation.ValueAnimator
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.R
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.databinding.ItemTaskBinding
import com.example.studentorganizer.ui.timetable.TimetableFormat

class TasksAdapter(
    private val onToggle: (Task, Boolean) -> Unit,
    private val onItemClick: (TaskItem) -> Unit,     // kept for edit dialog (long-press mapped here)
    private val onItemLongClick: (TaskItem) -> Unit  // kept for delete confirm
) : ListAdapter<TaskItem, TasksAdapter.ViewHolder>(Diff()) {

    /** Id of the currently expanded task card, or -1 when none is expanded. */
    private var expandedTaskId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskItem) {
            val ctx = binding.root.context
            val task = item.task
            val course = item.course

            // ── Colour bar (course colour or grey) ──────────────────────────
            binding.viewTaskBar.setBackgroundColor(
                course?.let { TimetableFormat.colorFor(it.courseId) }
                    ?: TimetableFormat.NO_COURSE_COLOR
            )

            // ── Collapsed section ───────────────────────────────────────────
            binding.tvTaskTitle.text = task.title
            binding.tvTaskTitle.paintFlags = strikeThrough(task.isCompleted, binding.tvTaskTitle.paintFlags)

            val courseLabel = course?.let {
                if (it.courseCode.isBlank()) it.courseName
                else "${it.courseName} (${it.courseCode})"
            }
            binding.tvTaskCourse.text = courseLabel
            binding.tvTaskCourse.visibility = if (course == null) View.GONE else View.VISIBLE

            // No "type" field in this Task model — hide the type chip
            binding.tvTaskType.visibility = View.GONE

            binding.tvTaskDates.text = TaskFormat.range(ctx, task.startMillis, task.endMillis)

            // ── Expanded section ────────────────────────────────────────────
            binding.tvExpandedTitle.text = task.title

            if (task.description.isBlank()) {
                binding.tvTaskDescription.visibility = View.GONE
            } else {
                binding.tvTaskDescription.text = task.description
                binding.tvTaskDescription.visibility = View.VISIBLE
            }

            binding.tvExpandedCourse.text = courseLabel ?: ctx.getString(R.string.tasks_no_course)
            binding.tvExpandedType.visibility = View.GONE
            // Hide the Type row in expanded panel (field not in Task model)
            binding.tvExpandedDates.text = TaskFormat.range(ctx, task.startMillis, task.endMillis)
            binding.tvExpandedStatus.text = if (task.isCompleted)
                ctx.getString(R.string.tasks_status_done)
            else
                ctx.getString(R.string.tasks_status_pending)

            // ── Checkbox ────────────────────────────────────────────────────
            binding.cbTaskDone.setOnCheckedChangeListener(null)
            binding.cbTaskDone.isChecked = task.isCompleted
            binding.cbTaskDone.setOnCheckedChangeListener { _, checked -> onToggle(task, checked) }

            // ── Delete button (inside expanded panel) ───────────────────────
            binding.btnDeleteTask.setOnClickListener { onItemLongClick(item) }

            // ── Expand / collapse state (no animation on initial bind) ──────
            val isExpanded = task.taskId == expandedTaskId
            setExpandedInstant(isExpanded)

            // ── Chevron rotation ────────────────────────────────────────────
            binding.ivExpandChevron.rotation = if (isExpanded) 270f else 90f

            // ── Card click → toggle expand ──────────────────────────────────
            binding.cardTask.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_ID.toInt()) return@setOnClickListener

                val previouslyExpandedId = expandedTaskId
                val clickedId = task.taskId

                if (previouslyExpandedId == clickedId) {
                    // collapse self
                    expandedTaskId = -1
                    animateExpand(false)
                } else {
                    // collapse previous card
                    if (previouslyExpandedId != -1) {
                        val prevPos = currentList.indexOfFirst { it.task.taskId == previouslyExpandedId }
                        if (prevPos != -1) notifyItemChanged(prevPos)
                    }
                    // expand this card
                    expandedTaskId = clickedId
                    animateExpand(true)
                }
            }

            // Long press still triggers edit dialog (original behaviour)
            binding.cardTask.setOnLongClickListener {
                onItemClick(item)
                true
            }
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private fun setExpandedInstant(expanded: Boolean) {
            binding.layoutExpanded.visibility = if (expanded) View.VISIBLE else View.GONE
            binding.dividerExpand.visibility = if (expanded) View.VISIBLE else View.GONE
        }

        private fun animateExpand(expand: Boolean) {
            val panel = binding.layoutExpanded
            val divider = binding.dividerExpand
            val chevron = binding.ivExpandChevron

            if (expand) {
                // Show the panel before measuring it
                divider.visibility = View.VISIBLE
                panel.visibility = View.VISIBLE

                // Reset height before measuring
                panel.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

                val parent = panel.parent as ViewGroup
                val availableWidth = parent.width -
                        parent.paddingLeft - parent.paddingRight

                panel.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        availableWidth,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    )
                )

                val targetH = panel.measuredHeight

                panel.layoutParams.height = 0
                panel.requestLayout()

                ValueAnimator.ofInt(0, targetH).apply {
                    duration = 220
                    interpolator = DecelerateInterpolator()

                    addUpdateListener { anim ->
                        panel.layoutParams.height = anim.animatedValue as Int
                        panel.requestLayout()
                    }

                    doOnEnd {
                        panel.layoutParams.height =
                            ViewGroup.LayoutParams.WRAP_CONTENT
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

                    doOnEnd {
                        panel.visibility = View.GONE
                        panel.layoutParams.height =
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        divider.visibility = View.GONE
                    }

                    start()
                }
            }

            val toAngle = if (expand) 270f else 90f
            chevron.animate()
                .rotation(toAngle)
                .setDuration(200)
                .start()
        }
        private fun strikeThrough(done: Boolean, flags: Int): Int =
            if (done) flags or Paint.STRIKE_THRU_TEXT_FLAG
            else flags and Paint.STRIKE_THRU_TEXT_FLAG.inv()


    }

    // ─── DiffUtil ──────────────────────────────────────────────────────────────

    class Diff : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(a: TaskItem, b: TaskItem) = a.task.taskId == b.task.taskId
        override fun areContentsTheSame(a: TaskItem, b: TaskItem) = a == b
    }
}

// Tiny Kotlin extension to avoid importing AnimatorListenerAdapter everywhere
private fun ValueAnimator.doOnEnd(block: () -> Unit) {
    addListener(object : android.animation.AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: android.animation.Animator) = block()
    })
}

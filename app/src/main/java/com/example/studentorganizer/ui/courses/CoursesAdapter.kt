package com.example.studentorganizer.ui.courses

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.databinding.ItemCourseBinding
import com.example.studentorganizer.ui.timetable.TimetableFormat

class CoursesAdapter(
    private val onCourseClick: (Course) -> Unit,       // "View Notes" / original card click → navigate
    private val onCourseLongClick: (Course) -> Unit    // confirm-delete dialog
) : ListAdapter<Course, CoursesAdapter.CourseViewHolder>(CourseDiff()) {

    /** Id of the currently expanded course card, or -1 when none is expanded. */
    private var expandedCourseId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            // ── Colour accent bar ───────────────────────────────────────────
            binding.viewCourseBar.setBackgroundColor(
                TimetableFormat.colorFor(course.courseId)
            )

            // ── Collapsed section ───────────────────────────────────────────
            binding.tvCourseName.text = course.courseName
            binding.tvCourseCode.text = course.courseCode
            binding.tvCreditHoursCompact.text =
                "${course.creditHours} ${if (course.creditHours == 1) "credit" else "credits"}"

            // ── Expanded section ────────────────────────────────────────────
            binding.tvCourseExpandedName.text = course.courseName
            binding.tvCourseExpandedCode.text = course.courseCode
            binding.tvInstructorName.text =
                course.instructorName.ifBlank { "—" }
            binding.tvCreditHours.text = course.creditHours.toString()

            // ── Action buttons (inside expanded panel) ──────────────────────
            binding.btnViewNotes.setOnClickListener { onCourseClick(course) }
            binding.btnDeleteCourse.setOnClickListener { onCourseLongClick(course) }

            // ── Expand / collapse state (instant on rebind) ─────────────────
            val isExpanded = course.courseId == expandedCourseId
            setExpandedInstant(isExpanded)
            binding.ivCourseChevron.rotation = if (isExpanded) 270f else 90f

            // ── Card click → toggle expand ──────────────────────────────────
            binding.cardCourse.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener

                val previousId = expandedCourseId
                val clickedId = course.courseId

                if (previousId == clickedId) {
                    expandedCourseId = -1
                    animateExpand(false)
                } else {
                    if (previousId != -1) {
                        val prevPos = currentList.indexOfFirst { it.courseId == previousId }
                        if (prevPos != -1) notifyItemChanged(prevPos)
                    }
                    expandedCourseId = clickedId
                    animateExpand(true)
                }
            }

            // Long-press → delete confirm (original behaviour preserved)
            binding.cardCourse.setOnLongClickListener {
                onCourseLongClick(course)
                true
            }
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private fun setExpandedInstant(expanded: Boolean) {
            binding.layoutCourseExpanded.visibility = if (expanded) View.VISIBLE else View.GONE
            binding.dividerCourseExpand.visibility = if (expanded) View.VISIBLE else View.GONE
        }


        private fun animateExpand(expand: Boolean) {
            val panel = binding.layoutCourseExpanded
            val divider = binding.dividerCourseExpand
            val chevron = binding.ivCourseChevron

            if (expand) {
                divider.visibility = View.VISIBLE
                panel.visibility = View.VISIBLE

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

                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            panel.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            panel.requestLayout()
                        }
                    })

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
                            panel.layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            divider.visibility = View.GONE
                        }
                    })

                    start()
                }
            }

            chevron.animate()
                .rotation(if (expand) 270f else 90f)
                .setDuration(200)
                .start()
        }
    }

    // ─── DiffUtil ──────────────────────────────────────────────────────────────

    class CourseDiff : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(a: Course, b: Course) = a.courseId == b.courseId
        override fun areContentsTheSame(a: Course, b: Course) = a == b
    }
}

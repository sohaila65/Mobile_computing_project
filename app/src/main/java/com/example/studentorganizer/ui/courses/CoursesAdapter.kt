package com.example.studentorganizer.ui.courses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.databinding.ItemCourseBinding

class CoursesAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onCourseLongClick: (Course) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    private var courses: List<Course> = emptyList()

    fun submitList(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    inner class CourseViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.tvCourseName.text = course.courseName
            binding.tvCourseCode.text = course.courseCode
            binding.tvInstructorName.text = "Instructor: ${course.instructorName}"
            binding.tvCreditHours.text = "Credits: ${course.creditHours}"

            binding.root.setOnClickListener {
                onCourseClick(course)
            }
            binding.root.setOnLongClickListener {
                onCourseLongClick(course)
                true
            }
        }
    }
}

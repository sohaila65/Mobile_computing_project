package com.example.studentorganizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.databinding.FragmentCoursesBinding
import com.example.studentorganizer.ui.courses.CourseViewModel
import com.example.studentorganizer.ui.courses.CoursesAdapter


class CoursesFragment : Fragment(R.layout.fragment_courses) {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val courseViewModel: CourseViewModel by viewModels()
    private lateinit var coursesAdapter: CoursesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCoursesBinding.bind(view)

        setupRecyclerView()

        courseViewModel.allCourses.observe(viewLifecycleOwner) { courses ->
            coursesAdapter.submitList(courses)
        }

        binding.fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
    }

    private fun setupRecyclerView() {
        coursesAdapter = CoursesAdapter(
            onCourseClick = { course ->
                val bundle = Bundle().apply {
                    putInt("courseId", course.courseId)
                    putString("courseName", course.courseName)
                }
                findNavController().navigate(
                    R.id.notesFragment,
                    bundle
                )
            },
            onCourseLongClick = { course ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Course")
                    .setMessage("Are you sure you want to delete ${course.courseName}? Its notes, timetable sessions and tasks will be removed too.")
                    .setPositiveButton("Delete") { _, _ ->
                        courseViewModel.delete(course)
                        Toast.makeText(requireContext(), "${course.courseName} deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvCourses.apply {
            adapter = coursesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_course, null)

        val etCourseName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etCourseCode = dialogView.findViewById<EditText>(R.id.etCourseCode)
        val etInstructorName = dialogView.findViewById<EditText>(R.id.etInstructorName)
        val etCreditHours = dialogView.findViewById<EditText>(R.id.etCreditHours)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Course")
            .setView(dialogView)
            .setPositiveButton("Add", null) // real handler below keeps the dialog open when input is invalid
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val courseName = etCourseName.text.toString().trim()
            val courseCode = etCourseCode.text.toString().trim()
            val instructorName = etInstructorName.text.toString().trim()
            val creditHours = etCreditHours.text.toString().trim().toIntOrNull()

            if (courseName.isNotEmpty() && courseCode.isNotEmpty() && creditHours != null) {
                courseViewModel.insert(
                    Course(
                        courseName = courseName,
                        courseCode = courseCode,
                        instructorName = instructorName,
                        creditHours = creditHours
                    )
                )
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

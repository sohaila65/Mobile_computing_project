package com.example.studentorganizer

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.studentorganizer.databinding.FragmentHomeBinding
import com.example.studentorganizer.databinding.ItemHomeBinding
import com.example.studentorganizer.ui.home.HomeState
import com.example.studentorganizer.ui.home.HomeViewModel
import com.example.studentorganizer.ui.home.NextClass
import com.example.studentorganizer.ui.tasks.TaskFormat
import com.example.studentorganizer.ui.timetable.TimetableFormat
import java.text.DateFormatSymbols
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var latest: HomeState? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        binding.btnViewTasks.setOnClickListener { goTo(R.id.tasksFragment) }
        binding.btnViewTimetable.setOnClickListener { goTo(R.id.timetableFragment) }
        binding.tasksFragment.setOnClickListener { goTo(R.id.tasksFragment) }
        binding.notesFragment.setOnClickListener { goTo(R.id.notesFragment) }
        binding.coursesFragment.setOnClickListener { goTo(R.id.coursesFragment) }


        viewModel.state.observe(viewLifecycleOwner) { state ->
            latest = state
            render(state)
        }
    }
    private fun goTo(destinationID: Int){
        findNavController().navigate(destinationID)
    }
    override fun onResume() {
        super.onResume()
        updateHeader()
        viewModel.refresh()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateHeader() {
        val now = System.currentTimeMillis()
        val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.setText(
            when {
                hour < 12 -> R.string.home_greeting_morning
                hour < 17 -> R.string.home_greeting_afternoon
                else -> R.string.home_greeting_evening
            }
        )
        binding.tvDate.text = DateUtils.formatDateTime(
            requireContext(), now,
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_DATE
        )
    }


    private fun render(state: HomeState) {
        binding.tvStatTasks.text = state.openTaskCount.toString()
        binding.tvStatCourses.text = state.courseCount.toString()
        binding.tvStatNotes.text = state.noteCount.toString()

        renderTodayTasks(state)
        renderNextClass(state.nextClass)
    }

    private fun renderTodayTasks(state: HomeState) {
        val container = binding.layoutTodayTasks
        container.removeAllViews()

        state.todayTasks.take(MAX_TASKS_SHOWN).forEach { task ->
            val row = ItemHomeBinding.inflate(layoutInflater, container, false)
            row.tvHomeTaskTitle.text = task.title
            row.tvHomeTaskTime.text = TaskFormat.range(requireContext(), task.startMillis, task.endMillis)
            container.addView(row.root)
        }

        binding.tvTodayTasksEmpty.visibility =
            if (state.todayTasks.isEmpty()) View.VISIBLE else View.GONE

        val hidden = state.todayTasks.size - MAX_TASKS_SHOWN
        binding.tvTodayTasksMore.visibility = if (hidden > 0) View.VISIBLE else View.GONE
        if (hidden > 0) binding.tvTodayTasksMore.text = getString(R.string.home_more_tasks, hidden)
    }



    private fun renderNextClass(next: NextClass?) {
        val hasClass = next != null
        binding.tvNextClassEmpty.visibility = if (hasClass) View.GONE else View.VISIBLE
        binding.tvNextClassName.visibility = if (hasClass) View.VISIBLE else View.GONE
        binding.tvNextClassWhen.visibility = if (hasClass) View.VISIBLE else View.GONE
        if (next == null) {
            binding.tvNextClassWhere.visibility = View.GONE
            return
        }

        val entry = next.item.entry
        binding.tvNextClassName.text = next.item.course.courseName
        binding.tvNextClassWhen.text = whenLabel(next)

        val place = entry.location.trim()
        binding.tvNextClassWhere.visibility = if (place.isEmpty()) View.GONE else View.VISIBLE
        binding.tvNextClassWhere.text = getString(R.string.home_next_class_where, place)
    }

    private fun whenLabel(next: NextClass): String {
        val entry = next.item.entry
        val day = when {
            next.isNow -> getString(R.string.home_next_class_now)
            next.daysAhead == 0 -> getString(R.string.home_next_class_today)
            next.daysAhead == 1 -> getString(R.string.home_next_class_tomorrow)
            else -> DateFormatSymbols.getInstance().weekdays[entry.dayOfWeek]
        }
        val time = TimetableFormat.range(requireContext(), entry.startMinutes, entry.endMinutes)
        return getString(R.string.home_next_class_when, day, time)
    }



    private fun buildShareText(): String {
        val state = latest ?: return getString(R.string.app_name)
        val tasksLine = getString(
            R.string.home_share_text_tasks, state.openTaskCount, state.todayTasks.size
        )
        val classLine = state.nextClass?.let {
            getString(R.string.home_share_text_next, it.item.course.courseName, whenLabel(it))
        } ?: getString(R.string.home_share_text_no_class)
        return "$tasksLine\n$classLine"
    }
    private companion object {
        const val MAX_TASKS_SHOWN = 3
    }
}
package com.example.studentorganizer

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.SaveResult
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskItem
import com.example.studentorganizer.data.TaskRules
import com.example.studentorganizer.data.TimetableEntry
import com.example.studentorganizer.data.TimetableRules
import com.example.studentorganizer.databinding.FragmentTimetableBinding
import com.example.studentorganizer.ui.tasks.TaskDialog
import com.example.studentorganizer.ui.tasks.TaskViewModel
import com.example.studentorganizer.ui.timetable.DayRow
import com.example.studentorganizer.ui.timetable.TimetableAdapter
import com.example.studentorganizer.ui.timetable.TimetableDays
import com.example.studentorganizer.ui.timetable.TimetableFormat
import com.example.studentorganizer.ui.timetable.TimetableItem
import com.example.studentorganizer.ui.timetable.TimetableViewModel
import com.google.android.material.tabs.TabLayout
import java.text.DateFormatSymbols
import java.util.Calendar

class TimetableFragment : Fragment(R.layout.fragment_timetable) {

    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimetableViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var timetableAdapter: TimetableAdapter

    // The week always runs Saturday -> Friday (Calendar day constants).
    private val days = TimetableDays.DAY_ORDER
    private val dayNames: Array<String> = DateFormatSymbols.getInstance().weekdays
    private val shortDayNames: Array<String> = DateFormatSymbols.getInstance().shortWeekdays

    private var weekOffset = 0
    private var selectedIndex = TimetableDays.todayIndex()

    private var latestSessions: List<TimetableItem> = emptyList()
    private var latestTasks: List<TaskItem> = emptyList()
    private var latestCourses: List<Course>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTimetableBinding.bind(view)

        savedInstanceState?.let {
            weekOffset = it.getInt(STATE_WEEK_OFFSET, 0)
            selectedIndex = it.getInt(STATE_SELECTED_INDEX, selectedIndex)
        }

        setupRecyclerView() // must exist before the first tab selection triggers render()
        setupTabs()
        setupWeekHeader()
        updateWeekLabels()

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            latestCourses = courses
            render()
        }
        viewModel.items.observe(viewLifecycleOwner) { items ->
            latestSessions = items
            render()
        }
        taskViewModel.items.observe(viewLifecycleOwner) { items ->
            latestTasks = items
            render()
        }

        binding.fabAddSession.setOnClickListener { showAddChooser() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_WEEK_OFFSET, weekOffset)
        outState.putInt(STATE_SELECTED_INDEX, selectedIndex)
    }

    // ---------------------------------------------------------------- week / tabs

    private fun weekStartMillis() = TimetableDays.weekStartMillis(System.currentTimeMillis(), weekOffset)

    private fun selectedDayStartMillis() = TimetableDays.dayStartMillis(weekStartMillis(), selectedIndex)

    private fun setupTabs() {
        days.forEach { _ -> binding.tabDays.addTab(binding.tabDays.newTab()) }
        binding.tabDays.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedIndex = tab.position
                render()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        selectDayTab(selectedIndex)
    }

    private fun selectDayTab(index: Int) {
        binding.tabDays.getTabAt(index)?.select()
    }

    private fun setupWeekHeader() {
        binding.btnPrevWeek.setOnClickListener {
            weekOffset--
            updateWeekLabels()
            render()
        }
        binding.btnNextWeek.setOnClickListener {
            weekOffset++
            updateWeekLabels()
            render()
        }
        binding.tvWeekRange.setOnClickListener {
            weekOffset = 0
            selectedIndex = TimetableDays.todayIndex()
            updateWeekLabels()
            selectDayTab(selectedIndex)
            render()
        }
    }

    /** Updates the "Nov 14 – Nov 20" header and puts the date number on each day tab. */
    private fun updateWeekLabels() {
        val weekStart = weekStartMillis()
        val weekEnd = TimetableDays.dayStartMillis(weekStart, days.size - 1)
        binding.tvWeekRange.text = DateUtils.formatDateRange(
            requireContext(), weekStart, weekEnd,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH
        )
        days.indices.forEach { i ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = TimetableDays.dayStartMillis(weekStart, i)
            }
            binding.tabDays.getTabAt(i)?.text =
                "${shortDayNames[days[i]]}\n${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }

    // ---------------------------------------------------------------- list

    private fun setupRecyclerView() {
        timetableAdapter = TimetableAdapter(
            onItemClick = { row ->
                when (row) {
                    is DayRow.Session -> showEntryDialog(row.item)
                    is DayRow.TaskEntry -> showTaskDialog(row.item.task)
                }
            },
            onItemLongClick = { row ->
                when (row) {
                    is DayRow.Session -> confirmDeleteSession(row.item)
                    is DayRow.TaskEntry -> confirmDeleteTask(row.item.task)
                }
            }
        )
        binding.rvTimetable.adapter = timetableAdapter
    }

    private fun render() {
        if (_binding == null) return
        val rows = TimetableDays.build(
            selectedDayStartMillis(), days[selectedIndex], latestSessions, latestTasks
        )
        timetableAdapter.submitList(rows)

        val courses = latestCourses
        val message = when {
            courses == null -> null // still loading
            rows.isNotEmpty() -> null
            courses.isEmpty() -> getString(R.string.timetable_no_courses)
            else -> getString(R.string.timetable_empty_day)
        }
        binding.tvTimetableEmpty.text = message
        binding.tvTimetableEmpty.visibility = if (message == null) View.GONE else View.VISIBLE
    }

    // ---------------------------------------------------------------- add / edit / delete

    /** The + button: manually add either a weekly class session or a dated task. */
    private fun showAddChooser() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.timetable_add_what)
            .setItems(
                arrayOf(getString(R.string.timetable_add_class), getString(R.string.timetable_add_task))
            ) { _, which ->
                if (which == 0) {
                    if (latestCourses.isNullOrEmpty()) toast(getString(R.string.timetable_no_courses))
                    else showEntryDialog(null)
                } else {
                    showTaskDialog(null)
                }
            }
            .show()
    }

    private fun showTaskDialog(existing: Task?) {
        // New tasks default to 9:00 on the day that is currently open.
        val defaultStart = TaskRules.withTime(selectedDayStartMillis(), 9, 0)
        TaskDialog.show(requireContext(), latestCourses.orEmpty(), existing, defaultStart) { task ->
            taskViewModel.save(task)
        }
    }

    private fun confirmDeleteTask(task: Task) {
        val ctx = requireContext()
        AlertDialog.Builder(ctx)
            .setTitle(R.string.tasks_delete_title)
            .setMessage(getString(R.string.tasks_delete_message, task.title))
            .setPositiveButton(R.string.tasks_delete) { _, _ ->
                taskViewModel.delete(task)
                Toast.makeText(ctx, R.string.tasks_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.tasks_cancel, null)
            .show()
    }

    private fun confirmDeleteSession(item: TimetableItem) {
        val ctx = requireContext()
        val range = TimetableFormat.range(ctx, item.entry.startMinutes, item.entry.endMinutes)
        AlertDialog.Builder(ctx)
            .setTitle(R.string.timetable_delete_title)
            .setMessage(
                getString(
                    R.string.timetable_delete_message,
                    item.course.courseName,
                    dayNames[item.entry.dayOfWeek],
                    range
                )
            )
            .setPositiveButton(R.string.timetable_delete) { _, _ ->
                viewModel.delete(item.entry)
                Toast.makeText(ctx, R.string.timetable_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.timetable_cancel, null)
            .show()
    }

    /** Opens the add dialog ([existing] == null) or the edit dialog for a class session. */
    private fun showEntryDialog(existing: TimetableItem?) {
        val ctx = requireContext()
        val courses = latestCourses.orEmpty()
        if (courses.isEmpty()) return

        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_timetable_entry, null)
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerCourse)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartTime)
        val btnEnd = dialogView.findViewById<Button>(R.id.btnEndTime)
        val etLocation = dialogView.findViewById<EditText>(R.id.etLocation)

        spinnerCourse.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_dropdown_item,
            courses.map { if (it.courseCode.isBlank()) it.courseName else "${it.courseName} (${it.courseCode})" }
        )
        spinnerDay.adapter = ArrayAdapter(
            ctx, android.R.layout.simple_spinner_dropdown_item, days.map { dayNames[it] }
        )

        val entry = existing?.entry
        var start = entry?.startMinutes ?: DEFAULT_START
        var end = entry?.endMinutes ?: DEFAULT_END
        spinnerCourse.setSelection(
            courses.indexOfFirst { it.courseId == entry?.courseId }.coerceAtLeast(0)
        )
        spinnerDay.setSelection(days.indexOf(entry?.dayOfWeek ?: days[selectedIndex]).coerceAtLeast(0))
        etLocation.setText(entry?.location.orEmpty())

        fun refreshTimeButtons() {
            btnStart.text = "${getString(R.string.timetable_pick_start)}: ${TimetableFormat.time(ctx, start)}"
            btnEnd.text = "${getString(R.string.timetable_pick_end)}: ${TimetableFormat.time(ctx, end)}"
        }
        refreshTimeButtons()

        btnStart.setOnClickListener {
            TimePickerDialog(ctx, { _, h, m ->
                start = h * 60 + m
                if (end <= start) end = minOf(start + 60, TimetableRules.MINUTES_PER_DAY)
                refreshTimeButtons()
            }, start / 60, start % 60, DateFormat.is24HourFormat(ctx)).show()
        }
        btnEnd.setOnClickListener {
            TimePickerDialog(ctx, { _, h, m ->
                val picked = h * 60 + m
                // 00:00 as an end time means "end of day".
                end = if (picked == 0) TimetableRules.MINUTES_PER_DAY else picked
                refreshTimeButtons()
            }, (end / 60) % 24, end % 60, DateFormat.is24HourFormat(ctx)).show()
        }

        val dialog = AlertDialog.Builder(ctx)
            .setTitle(if (existing == null) R.string.timetable_add_session else R.string.timetable_edit_session)
            .setView(dialogView)
            .setPositiveButton(R.string.timetable_save, null) // real handler below keeps the dialog open on errors
            .setNegativeButton(R.string.timetable_cancel, null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val candidate = TimetableEntry(
                entryId = entry?.entryId ?: 0,
                courseId = courses[spinnerCourse.selectedItemPosition].courseId,
                dayOfWeek = days[spinnerDay.selectedItemPosition],
                startMinutes = start,
                endMinutes = end,
                location = etLocation.text.toString().trim()
            )
            viewModel.save(candidate) { result ->
                if (_binding == null) return@save
                when (result) {
                    is SaveResult.Saved -> {
                        selectDayTab(days.indexOf(candidate.dayOfWeek))
                        dialog.dismiss()
                    }
                    is SaveResult.Invalid -> toast(
                        if (result.problem == TimetableRules.Problem.END_NOT_AFTER_START)
                            getString(R.string.timetable_err_time)
                        else getString(R.string.timetable_err_invalid)
                    )
                    is SaveResult.Conflict -> {
                        val other = result.other
                        val name = latestCourses.orEmpty()
                            .firstOrNull { it.courseId == other.courseId }?.courseName.orEmpty()
                        toast(
                            getString(
                                R.string.timetable_err_conflict, name,
                                TimetableFormat.range(ctx, other.startMinutes, other.endMinutes)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val STATE_WEEK_OFFSET = "week_offset"
        const val STATE_SELECTED_INDEX = "selected_index"
        const val DEFAULT_START = 9 * 60
        const val DEFAULT_END = 10 * 60 + 30
    }
}

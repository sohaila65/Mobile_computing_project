package com.example.studentorganizer.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.studentorganizer.R
import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.Task
import com.example.studentorganizer.data.TaskRules
import java.util.Calendar
import java.util.Date

/** Add / edit dialog shared by the Tasks tab and the Timetable tab. */
object TaskDialog {

    /**
     * @param existing the task to edit, or null to create a new one
     * @param defaultStartMillis start suggested for a new task
     * @param onSave called with a valid task; the dialog then closes
     */
    fun show(
        context: Context,
        courses: List<Course>,
        existing: Task?,
        defaultStartMillis: Long,
        onSave: (Task) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_task, null)
        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val etDescription = view.findViewById<EditText>(R.id.etTaskDescription)
        val spinnerCourse = view.findViewById<Spinner>(R.id.spinnerTaskCourse)
        val btnStartDate = view.findViewById<Button>(R.id.btnTaskStartDate)
        val btnStartTime = view.findViewById<Button>(R.id.btnTaskStartTime)
        val btnEndDate = view.findViewById<Button>(R.id.btnTaskEndDate)
        val btnEndTime = view.findViewById<Button>(R.id.btnTaskEndTime)

        spinnerCourse.adapter = ArrayAdapter(
            context, android.R.layout.simple_spinner_dropdown_item,
            listOf(context.getString(R.string.tasks_no_course)) +
                courses.map { if (it.courseCode.isBlank()) it.courseName else "${it.courseName} (${it.courseCode})" }
        )
        val courseIndex = courses.indexOfFirst { it.courseId == existing?.courseId }
        spinnerCourse.setSelection(if (courseIndex < 0) 0 else courseIndex + 1)

        etTitle.setText(existing?.title.orEmpty())
        etDescription.setText(existing?.description.orEmpty())

        var start = existing?.startMillis ?: defaultStartMillis
        var end = existing?.endMillis ?: (start + TaskRules.HOUR_MS)

        val dateFmt = DateFormat.getMediumDateFormat(context)
        val timeFmt = DateFormat.getTimeFormat(context)

        fun refresh() {
            btnStartDate.text = dateFmt.format(Date(start))
            btnStartTime.text = timeFmt.format(Date(start))
            btnEndDate.text = dateFmt.format(Date(end))
            btnEndTime.text = timeFmt.format(Date(end))
        }
        refresh()

        // Moving the start keeps the duration, so the end can never fall behind the start by accident.
        fun moveStart(newStart: Long) {
            val duration = end - start
            start = newStart
            end = start + duration
            refresh()
        }

        fun pickDate(current: Long, onPicked: (Int, Int, Int) -> Unit) {
            val c = Calendar.getInstance().apply { timeInMillis = current }
            DatePickerDialog(
                context, { _, y, m, d -> onPicked(y, m, d) },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        fun pickTime(current: Long, onPicked: (Int, Int) -> Unit) {
            val c = Calendar.getInstance().apply { timeInMillis = current }
            TimePickerDialog(
                context, { _, h, m -> onPicked(h, m) },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(context)
            ).show()
        }

        btnStartDate.setOnClickListener {
            pickDate(start) { y, m, d -> moveStart(TaskRules.withDate(start, y, m, d)) }
        }
        btnStartTime.setOnClickListener {
            pickTime(start) { h, m -> moveStart(TaskRules.withTime(start, h, m)) }
        }
        btnEndDate.setOnClickListener {
            pickDate(end) { y, m, d -> end = TaskRules.withDate(end, y, m, d); refresh() }
        }
        btnEndTime.setOnClickListener {
            pickTime(end) { h, m -> end = TaskRules.withTime(end, h, m); refresh() }
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(if (existing == null) R.string.tasks_add else R.string.tasks_edit)
            .setView(view)
            .setPositiveButton(R.string.tasks_save, null) // real handler below keeps the dialog open on errors
            .setNegativeButton(R.string.tasks_cancel, null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selected = spinnerCourse.selectedItemPosition
            val task = Task(
                taskId = existing?.taskId ?: 0,
                title = etTitle.text.toString().trim(),
                description = etDescription.text.toString().trim(),
                courseId = if (selected <= 0) null else courses[selected - 1].courseId,
                startMillis = start,
                endMillis = end,
                isCompleted = existing?.isCompleted ?: false
            )
            when (TaskRules.validate(task)) {
                TaskRules.Problem.EMPTY_TITLE -> etTitle.error = context.getString(R.string.tasks_err_title)
                TaskRules.Problem.END_NOT_AFTER_START ->
                    Toast.makeText(context, R.string.tasks_err_time, Toast.LENGTH_SHORT).show()
                null -> {
                    onSave(task)
                    dialog.dismiss()
                }
            }
        }
    }
}

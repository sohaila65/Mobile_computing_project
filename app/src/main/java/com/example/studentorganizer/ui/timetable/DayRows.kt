package com.example.studentorganizer.ui.timetable

import com.example.studentorganizer.data.TaskItem
import java.util.Calendar

/** One line in a day's timetable: a recurring class session or a dated task. */
sealed class DayRow {
    abstract val startMinutes: Int
    abstract val endMinutes: Int
    abstract val key: String

    data class Session(val item: TimetableItem) : DayRow() {
        override val startMinutes get() = item.entry.startMinutes
        override val endMinutes get() = item.entry.endMinutes
        override val key get() = "s${item.entry.entryId}"
    }

    /** [startsToday]/[endsToday] are false when the task began before / continues after this day. */
    data class TaskEntry(
        val item: TaskItem,
        override val startMinutes: Int,
        override val endMinutes: Int,
        val startsToday: Boolean,
        val endsToday: Boolean
    ) : DayRow() {
        override val key get() = "t${item.task.taskId}"
    }
}

/** Week/day maths for the timetable. The week always runs Saturday to Friday. */
object TimetableDays {

    /** Calendar day constants in display order: Saturday first, Friday last. */
    val DAY_ORDER: List<Int> = listOf(
        Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    fun todayIndex(nowMillis: Long = System.currentTimeMillis()): Int {
        val dow = Calendar.getInstance().apply { timeInMillis = nowMillis }.get(Calendar.DAY_OF_WEEK)
        return DAY_ORDER.indexOf(dow)
    }

    /** Midnight at the start of the Saturday of the week containing [nowMillis], shifted by [weekOffset] weeks. */
    fun weekStartMillis(nowMillis: Long, weekOffset: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val sinceSaturday = (get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
            add(Calendar.DAY_OF_MONTH, -sinceSaturday + weekOffset * 7)
        }.timeInMillis

    /** Midnight at the start of day [index] (0 = Saturday .. 6 = Friday) of the week starting at [weekStartMillis]. */
    fun dayStartMillis(weekStartMillis: Long, index: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = weekStartMillis
            add(Calendar.DAY_OF_MONTH, index)
        }.timeInMillis

    private fun minutesOfDay(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    /**
     * Everything happening on the day that starts at [dayStartMillis]: the weekly class sessions for
     * [dayOfWeek] plus every task whose start..end range touches that date, sorted by start time.
     */
    fun build(
        dayStart: Long,
        dayOfWeek: Int,
        sessions: List<TimetableItem>,
        tasks: List<TaskItem>
    ): List<DayRow> {
        val dayEnd = dayStartMillis(dayStart, 1)
        val rows = ArrayList<DayRow>()

        sessions.filter { it.entry.dayOfWeek == dayOfWeek }
            .forEach { rows.add(DayRow.Session(it)) }

        tasks.filter { it.task.startMillis < dayEnd && it.task.endMillis > dayStart }
            .forEach {
                val startsToday = it.task.startMillis >= dayStart
                val endsToday = it.task.endMillis <= dayEnd
                rows.add(
                    DayRow.TaskEntry(
                        item = it,
                        startMinutes = if (startsToday) minutesOfDay(it.task.startMillis) else 0,
                        endMinutes = when {
                            !endsToday || it.task.endMillis == dayEnd -> 24 * 60
                            else -> minutesOfDay(it.task.endMillis)
                        },
                        startsToday = startsToday,
                        endsToday = endsToday
                    )
                )
            }

        return rows.sortedWith(compareBy<DayRow>({ it.startMinutes }, { it.endMinutes }, { it.key }))
    }
}

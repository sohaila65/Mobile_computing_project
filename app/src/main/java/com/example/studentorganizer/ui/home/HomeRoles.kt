package com.example.studentorganizer.ui.home

import com.example.studentorganizer.data.Task
import com.example.studentorganizer.ui.timetable.TimetableItem
import java.util.Calendar

data class NextClass(val item: TimetableItem, val daysAhead: Int, val isNow: Boolean)

object HomeRules {
    private const val MINUTES_PER_DAY = 24 * 60
    fun startOfDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    private fun startOfNextDay(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = startOfDay(millis)
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis


    fun openTasks(tasks: List<Task>): List<Task> = tasks.filter { !it.isCompleted }

    fun completedTasks(tasks: List<Task>): List<Task> =tasks.filter{it.isCompleted}

    fun completionPercent(tasks: List<Task>): Int =
        if(tasks.isEmpty()) 0
        else
            tasks.count { it.isCompleted } * 100 / tasks.size
    fun tasksDueToday(tasks: List<Task>, now: Long): List<Task> {
        val dayStart = startOfDay(now)
        val dayEnd = startOfNextDay(now)
        return tasks
            .filter { !it.isCompleted && it.startMillis < dayEnd && it.endMillis > dayStart }
            .sortedBy { it.startMillis }
    }




    fun nextClass(sessions: List<TimetableItem>, now: Long): NextClass? {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val today = cal.get(Calendar.DAY_OF_WEEK)
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return sessions
            .map { item ->
                val entry = item.entry
                var daysAhead = (entry.dayOfWeek - today + 7) % 7
                val isNow = daysAhead == 0 &&
                        nowMinutes >= entry.startMinutes && nowMinutes < entry.endMinutes
                if (daysAhead == 0 && nowMinutes >= entry.endMinutes) daysAhead = 7
                NextClass(item, daysAhead, isNow)
            }
            .minWithOrNull(
                compareBy<NextClass>(
                    { minutesUntil(it, nowMinutes) },
                    { it.item.entry.startMinutes }
                )
            )
    }
    private fun minutesUntil(next: NextClass, nowMinutes: Int): Int =
        if (next.isNow) 0
        else next.daysAhead * MINUTES_PER_DAY + next.item.entry.startMinutes - nowMinutes
}
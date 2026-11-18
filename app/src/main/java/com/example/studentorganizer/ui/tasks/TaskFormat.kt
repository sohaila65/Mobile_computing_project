package com.example.studentorganizer.ui.tasks

import android.content.Context
import android.text.format.DateFormat
import com.example.studentorganizer.data.TaskRules
import java.util.Date

object TaskFormat {
    fun range(context: Context, start: Long, end: Long): String {
        val dateFmt = DateFormat.getMediumDateFormat(context)
        val timeFmt = DateFormat.getTimeFormat(context)
        val s = Date(start)
        val e = Date(end)
        return if (TaskRules.sameDay(start, end)) {
            "${dateFmt.format(s)} · ${timeFmt.format(s)} – ${timeFmt.format(e)}"
        } else {
            "${dateFmt.format(s)} ${timeFmt.format(s)} → ${dateFmt.format(e)} ${timeFmt.format(e)}"
        }
    }
}

package com.example.studentorganizer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Separate Room database that stores only the timetable sessions.
 * AppDatabase (courses + notes) is intentionally not modified.
 */
@Database(entities = [TimetableEntry::class], version = 1, exportSchema = false)
abstract class TimetableDatabase : RoomDatabase() {

    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: TimetableDatabase? = null

        fun getInstance(context: Context): TimetableDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimetableDatabase::class.java,
                    "timetable_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

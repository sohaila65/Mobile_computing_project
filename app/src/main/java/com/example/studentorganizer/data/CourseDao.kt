package com.example.studentorganizer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE courseId = :id")
    fun getCourseById(id: Int): Flow<Course?>

    @Query("SELECT * FROM courses WHERE :query = '' OR courseName LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%' OR instructorName LIKE '%' || :query || '%' OR CAST(creditHours AS TEXT) LIKE '%' || :query || '%'")
    fun searchCourses(query: String): Flow<List<Course>>
}

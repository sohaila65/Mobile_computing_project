package com.example.studentorganizer.data

class CourseRepository(private val courseDao: CourseDao) {
    suspend fun insertCourse(course: Course): Boolean {
        if (course.courseName.isBlank()) {
            return false
        }
        courseDao.insertCourse(course)
        return true
    }
}

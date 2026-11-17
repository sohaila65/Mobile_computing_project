package com.example.studentorganizer

import com.example.studentorganizer.data.Course
import com.example.studentorganizer.data.CourseDao
import com.example.studentorganizer.data.CourseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class CourseRepositoryTest {

    private lateinit var courseDao: CourseDao
    private lateinit var repository: CourseRepository

    @Before
    fun setup() {
        courseDao = mock()
        repository = CourseRepository(courseDao)
    }

    @Test
    fun insertCourse_withEmptyName_returnsFalseAndDoesNotCallDao() {
        runBlocking {
            // Arrange
            val invalidCourse = Course(
                courseName = "",
                courseCode = "CS101",
                instructorName = "John Doe",
                creditHours = 3
            )

            // Act
            val result = repository.insertCourse(invalidCourse)

            // Assert
            assertFalse(result)
            verifyNoInteractions(courseDao)
        }
    }

    @Test
    fun insertCourse_withValidName_returnsTrueAndCallsDao() {
        runBlocking {
            // Arrange
            val validCourse = Course(
                courseName = "Mathematics",
                courseCode = "MATH101",
                instructorName = "Jane Doe",
                creditHours = 4
            )

            // Act
            val result = repository.insertCourse(validCourse)

            // Assert
            assertTrue(result)
            verify(courseDao).insertCourse(validCourse)
        }
    }

}

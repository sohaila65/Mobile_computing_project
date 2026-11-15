package data

import androidx.room.*

data class TaskWithCourse(
    @Embedded val task: Task,
    @Relation(parentColumn = "courseId", entityColumn = "courseId")
    val course: Course
)

data class LectureWithCourse(
    @Embedded val lecture: Lecture,
    @Relation(parentColumn = "courseId", entityColumn = "courseId")
    val course: Course
)

data class NoteWithCourse(
    @Embedded val note: Note,
    @Relation(parentColumn = "courseId", entityColumn = "courseId")
    val course: Course?
)
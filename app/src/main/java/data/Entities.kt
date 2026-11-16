package data

import androidx.room.*

object TaskType {
    const val ASSIGNMENT = "ASSIGNMENT"
    const val QUIZ = "QUIZ"
    const val EXAM = "EXAM"
}

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val courseId: Int = 0,
    val name: String,
    val code: String? = null,
    val instructor: String? = null,
    val color: Int = 0xFF2196F3.toInt()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["courseId"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val taskId: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String? = null,
    val deadline: Long,
    val priority: Int = 1,
    val type: String = TaskType.EXAM,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["courseId"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("courseId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val courseId: Int? = null,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)


@Entity(
    tableName = "lectures",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["courseId"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class Lecture(
    @PrimaryKey(autoGenerate = true) val lectureId: Int = 0,
    val courseId: Int,
    val dayOfWeek: Int,
    val startMinutes: Int,
    val endMinutes: Int,
    val location: String? = null
)
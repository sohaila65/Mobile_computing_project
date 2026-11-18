package data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface CourseDao {
    @Insert suspend fun insert(course: Course)
    @Update suspend fun update(course: Course)
    @Delete suspend fun delete(course: Course)

    @Query("SELECT * FROM courses ORDER BY name")
    fun getAll(): Flow<List<Course>>
}

@Dao
interface TaskDao {
    @Insert suspend fun insert(task: Task)
    @Update suspend fun update(task: Task)
    @Delete suspend fun delete(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted, deadline")
    fun getAll(): Flow<List<TaskWithCourse>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE courseId = :courseId ORDER BY deadline")
    fun getByCourse(courseId: Int): Flow<List<TaskWithCourse>>


    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun pendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun completedCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline >= :now ORDER BY deadline LIMIT :limit")
    fun getUpcoming(now: Long, limit: Int = 5): Flow<List<TaskWithCourse>>


    @Transaction
    @Query("SELECT * FROM tasks WHERE type = 'EXAM' AND isCompleted = 0 AND deadline >= :now ORDER BY deadline LIMIT 1")
    fun nextExam(now: Long): Flow<TaskWithCourse?>
}

@Dao
interface NoteDao {
    @Insert suspend fun insert(note: Note)
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<NoteWithCourse>>

    @Transaction
    @Query("SELECT * FROM notes WHERE courseId = :courseId ORDER BY updatedAt DESC")
    fun getByCourse(courseId: Int): Flow<List<NoteWithCourse>>
}

@Dao
interface LectureDao {
    @Insert suspend fun insert(lecture: Lecture)
    @Update suspend fun update(lecture: Lecture)
    @Delete suspend fun delete(lecture: Lecture)

    @Transaction
    @Query("SELECT * FROM lectures ORDER BY dayOfWeek, startMinutes")
    fun getAll(): Flow<List<LectureWithCourse>>

    @Transaction
    @Query("SELECT * FROM lectures WHERE dayOfWeek = :day ORDER BY startMinutes")
    fun getByDay(day: Int): Flow<List<LectureWithCourse>>
}
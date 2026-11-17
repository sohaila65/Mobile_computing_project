package com.example.studentorganizer.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val courseId: Int,
    val title: String,
    val content: String,
    val tag: String,
    val createdAt: Long
)

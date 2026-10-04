package com.videogpt.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prompt: String,
    val outputPath: String,
    val createdAt: Long,
    val status: String = "completed",
    val durationSec: Int = 30,
    val aspectRatio: String = "16:9"
)

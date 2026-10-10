package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["name"]),
        Index(value = ["createdAt"])
    ]
)
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)

data class AlbumWithCount(
    @Embedded val album: Album,
    val mediaCount: Int = 0
)

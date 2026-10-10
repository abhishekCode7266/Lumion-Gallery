package com.example.data.db

import androidx.room.Entity
import androidx.room.Fts4
import com.example.data.model.MediaItem

/**
 * Room Full-Text Search (FTS4) virtual table indexing MediaItem metadata.
 * Enables fast full-text searching by name, date, tags, description, and category.
 */
@Entity(tableName = "media_items_fts")
@Fts4(contentEntity = MediaItem::class)
data class MediaItemFts(
    val name: String,
    val dateText: String,
    val tags: String,
    val description: String,
    val category: String
)

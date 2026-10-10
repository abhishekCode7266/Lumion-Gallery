package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_items",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["isTrash", "isVault", "dateTaken"]),
        Index(value = ["isFavorite"]),
        Index(value = ["albumId"]),
        Index(value = ["category"]),
        Index(value = ["trashedAt"])
    ]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val name: String,
    val mimeType: String = "image/jpeg",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateTaken: Long = System.currentTimeMillis(),
    val width: Int = 1920,
    val height: Int = 1080,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val trashedAt: Long? = null,
    val isVault: Boolean = false,
    val albumId: Long? = null,
    val category: String = "Camera",
    val description: String = "",
    val tags: String = "",
    val dateText: String = ""
) {
    fun withFormattedDate(): MediaItem {
        return if (dateText.isBlank()) {
            copy(dateText = formatDate(dateTaken))
        } else {
            this
        }
    }

    companion object {
        fun formatDate(timestamp: Long): String {
            return try {
                val date = java.util.Date(timestamp)
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd yyyy MMMM MMM d EEEE", java.util.Locale.ENGLISH)
                sdf.format(date)
            } catch (e: Exception) {
                ""
            }
        }
    }

    val isVideo: Boolean
        get() = mimeType.startsWith("video/") || name.endsWith(".mp4", ignoreCase = true) || name.endsWith(".mov", ignoreCase = true) || name.endsWith(".webm", ignoreCase = true)

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "Unknown size"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.1f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "0:00"
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

    val daysRemainingInTrash: Int
        get() {
            if (trashedAt == null) return 30
            val elapsedMs = System.currentTimeMillis() - trashedAt
            val daysElapsed = (elapsedMs / (1000L * 60 * 60 * 24)).toInt()
            return (30 - daysElapsed).coerceIn(0, 30)
        }

    val trashedDateFormatted: String
        get() {
            if (trashedAt == null) return "Recently"
            return try {
                val date = java.util.Date(trashedAt)
                val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH)
                sdf.format(date)
            } catch (e: Exception) {
                "Recently"
            }
        }
}

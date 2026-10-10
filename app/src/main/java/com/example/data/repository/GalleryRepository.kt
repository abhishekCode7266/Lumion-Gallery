package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.db.AlbumDao
import com.example.data.db.GalleryDatabase
import com.example.data.db.MediaDao
import com.example.data.model.Album
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

class GalleryRepository(private val context: Context) {
    private val db = GalleryDatabase.getInstance(context)
    private val mediaDao: MediaDao = db.mediaDao()
    private val albumDao: AlbumDao = db.albumDao()

    private val prefs = context.getSharedPreferences("lumina_gallery_prefs", Context.MODE_PRIVATE)

    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllActiveMedia()
    val favorites: Flow<List<MediaItem>> = mediaDao.getFavorites()
    val trashMedia: Flow<List<MediaItem>> = mediaDao.getTrashMedia()
    val vaultMedia: Flow<List<MediaItem>> = mediaDao.getVaultMedia()
    val albums: Flow<List<Album>> = albumDao.getAllAlbums()

    fun getMediaByAlbum(albumId: Long): Flow<List<MediaItem>> = mediaDao.getMediaByAlbum(albumId)
    fun getMediaByCategory(category: String): Flow<List<MediaItem>> = mediaDao.getMediaByCategory(category)

    suspend fun initDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val totalMedia = mediaDao.getTotalCount()
        if (totalMedia == 0) {
            // Seed starter albums
            val travelAlbumId = albumDao.insertAlbum(
                Album(name = "Travel & Nature", isSystem = false)
            )
            val architectureAlbumId = albumDao.insertAlbum(
                Album(name = "Architecture", isSystem = false)
            )
            val portraitsAlbumId = albumDao.insertAlbum(
                Album(name = "Portraits & People", isSystem = false)
            )

            val now = System.currentTimeMillis()
            val day = 86400000L

            // Curated initial photography showcase using Unsplash direct image links
            val starterItems = listOf(
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1600&q=80",
                    name = "IMG_Alpine_Reflection.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 3_450_000L,
                    dateAdded = now - (day * 1),
                    dateTaken = now - (day * 1),
                    width = 2560,
                    height = 1440,
                    isFavorite = true,
                    albumId = travelAlbumId,
                    category = "Camera",
                    description = "Emerald mountain lake surrounded by pine forests and misty peaks",
                    tags = "nature, mountain, lake, landscape"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=1600&q=80",
                    name = "IMG_Neon_City_Night.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 2_890_000L,
                    dateAdded = now - (day * 2),
                    dateTaken = now - (day * 2),
                    width = 2048,
                    height = 1536,
                    isFavorite = true,
                    albumId = architectureAlbumId,
                    category = "Camera",
                    description = "Vibrant neon reflections along rain-slicked city streets",
                    tags = "city, night, neon, architecture"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=1600&q=80",
                    name = "IMG_Desert_Golden_Hour.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 4_120_000L,
                    dateAdded = now - (day * 3),
                    dateTaken = now - (day * 3),
                    width = 3840,
                    height = 2160,
                    isFavorite = false,
                    albumId = travelAlbumId,
                    category = "Camera",
                    description = "Undulating sand dunes glowing under warm golden hour sunlight",
                    tags = "desert, sunset, dunes, golden hour"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1600&q=80",
                    name = "PORTRAIT_Studio_Glow.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 3_150_000L,
                    dateAdded = now - (day * 4),
                    dateTaken = now - (day * 4),
                    width = 2400,
                    height = 3000,
                    isFavorite = true,
                    albumId = portraitsAlbumId,
                    category = "Camera",
                    description = "Editorial beauty portrait with soft warm lighting",
                    tags = "portrait, fashion, model, lighting"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1600&q=80",
                    name = "IMG_Tropical_Coastline.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 5_240_000L,
                    dateAdded = now - (day * 5),
                    dateTaken = now - (day * 5),
                    width = 3840,
                    height = 2160,
                    isFavorite = false,
                    albumId = travelAlbumId,
                    category = "Camera",
                    description = "Turquoise waves washing over pristine white sandy beach",
                    tags = "beach, ocean, tropical, vacation"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1600&q=80",
                    name = "Screenshot_Code_Architecture.png",
                    mimeType = "image/png",
                    sizeBytes = 1_120_000L,
                    dateAdded = now - (day * 6),
                    dateTaken = now - (day * 6),
                    width = 1080,
                    height = 2400,
                    isFavorite = false,
                    albumId = null,
                    category = "Screenshots",
                    description = "Tech hardware circuits and processor diagram",
                    tags = "technology, processor, code, hardware"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1600&q=80",
                    name = "IMG_Misty_Forest_Dawn.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 3_780_000L,
                    dateAdded = now - (day * 7),
                    dateTaken = now - (day * 7),
                    width = 2800,
                    height = 1860,
                    isFavorite = false,
                    albumId = travelAlbumId,
                    category = "Camera",
                    description = "Fog rolling across dense evergreen forest canopy at sunrise",
                    tags = "forest, fog, dawn, trees, wilderness"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=1600&q=80",
                    name = "IMG_Glass_Skyscraper.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 2_640_000L,
                    dateAdded = now - (day * 8),
                    dateTaken = now - (day * 8),
                    width = 2100,
                    height = 3150,
                    isFavorite = false,
                    albumId = architectureAlbumId,
                    category = "Downloads",
                    description = "Looking straight up at mirrored glass financial towers",
                    tags = "modern, architecture, glass, building"
                ),
                MediaItem(
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    name = "VID_Chromecast_Blazes.mp4",
                    mimeType = "video/mp4",
                    sizeBytes = 15_420_000L,
                    dateAdded = now - (day * 9),
                    dateTaken = now - (day * 9),
                    width = 1920,
                    height = 1080,
                    durationMs = 15_000L,
                    isFavorite = true,
                    albumId = null,
                    category = "Videos",
                    description = "Sample high-definition motion video demo",
                    tags = "video, sample, showcase"
                ),
                MediaItem(
                    uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    name = "VID_Scenic_Escape.mp4",
                    mimeType = "video/mp4",
                    sizeBytes = 14_900_000L,
                    dateAdded = now - (day * 10),
                    dateTaken = now - (day * 10),
                    width = 1920,
                    height = 1080,
                    durationMs = 15_000L,
                    isFavorite = false,
                    albumId = null,
                    category = "Videos",
                    description = "Sample scenic escape motion video",
                    tags = "video, travel, nature"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1517849845537-4d257902454a?w=1600&q=80",
                    name = "IMG_Puppy_Park.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 2_890_000L,
                    dateAdded = now - (day * 12),
                    dateTaken = now - (day * 12),
                    width = 2400,
                    height = 1800,
                    isTrash = true,
                    trashedAt = now - (day * 3), // Trashed 3 days ago -> 27 days left
                    category = "Camera",
                    description = "Puppy playing on grass (deleted photo)",
                    tags = "dog, puppy, pet, park"
                ),
                MediaItem(
                    uri = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1600&q=80",
                    name = "IMG_Fresh_Salad_Bowl.jpg",
                    mimeType = "image/jpeg",
                    sizeBytes = 3_450_000L,
                    dateAdded = now - (day * 14),
                    dateTaken = now - (day * 14),
                    width = 2000,
                    height = 2000,
                    isTrash = true,
                    trashedAt = now - (day * 10), // Trashed 10 days ago -> 20 days left
                    category = "Camera",
                    description = "Fresh salad bowl with vegetables (deleted photo)",
                    tags = "food, salad, healthy, lunch"
                )
            )

            mediaDao.insertAll(starterItems.map { it.withFormattedDate() })

            // Set album covers
            albumDao.updateCover(travelAlbumId, starterItems[0].uri)
            albumDao.updateCover(architectureAlbumId, starterItems[1].uri)
            albumDao.updateCover(portraitsAlbumId, starterItems[3].uri)
        }
    }

    suspend fun importMediaUris(uris: List<Uri>, defaultCategory: String = "Downloads"): List<Long> = withContext(Dispatchers.IO) {
        val insertedIds = mutableListOf<Long>()
        for (uri in uris) {
            try {
                // Try to take persistable URI permission if applicable
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { }

                var displayName = "IMG_${System.currentTimeMillis()}.jpg"
                var sizeBytes = 0L
                val mimeType = context.contentResolver.getType(uri) ?: if (uri.toString().endsWith(".mp4", ignoreCase = true)) "video/mp4" else "image/jpeg"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) displayName = name
                        }
                        if (sizeIndex != -1) {
                            sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                }

                var width = 1920
                var height = 1080
                var durationMs = 0L

                val isVideo = mimeType.startsWith("video/") || displayName.endsWith(".mp4", ignoreCase = true)

                if (isVideo) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, uri)
                        val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                        val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                        val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                        if (w != null && w > 0) width = w
                        if (h != null && h > 0) height = h
                        if (dur != null) durationMs = dur
                        retriever.release()
                    } catch (_: Exception) { }
                } else {
                    try {
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream, null, options)
                            if (options.outWidth > 0 && options.outHeight > 0) {
                                width = options.outWidth
                                height = options.outHeight
                            }
                        }
                    } catch (_: Exception) { }
                }

                val item = MediaItem(
                    uri = uri.toString(),
                    name = displayName,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    dateAdded = System.currentTimeMillis(),
                    dateTaken = System.currentTimeMillis(),
                    width = width,
                    height = height,
                    durationMs = durationMs,
                    category = if (isVideo) "Videos" else defaultCategory
                )
                val id = mediaDao.insertMedia(item.withFormattedDate())
                insertedIds.add(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        insertedIds
    }

    suspend fun saveEditedPhoto(
        originalItem: MediaItem,
        editedBitmap: Bitmap,
        saveAsNewCopy: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val editsDir = File(context.filesDir, "gallery_edits").apply { mkdirs() }
        val filename = "EDIT_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val destFile = File(editsDir, filename)

        FileOutputStream(destFile).use { out ->
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val fileUri = Uri.fromFile(destFile).toString()
        val size = destFile.length()

        if (saveAsNewCopy) {
            val newItem = MediaItem(
                uri = fileUri,
                name = filename,
                mimeType = "image/jpeg",
                sizeBytes = size,
                dateAdded = System.currentTimeMillis(),
                dateTaken = System.currentTimeMillis(),
                width = editedBitmap.width,
                height = editedBitmap.height,
                category = "Edits",
                albumId = originalItem.albumId,
                description = "Edited version of ${originalItem.name}"
            )
            mediaDao.insertMedia(newItem.withFormattedDate())
        } else {
            val updated = originalItem.copy(
                uri = fileUri,
                name = filename,
                sizeBytes = size,
                width = editedBitmap.width,
                height = editedBitmap.height,
                category = "Edits"
            )
            mediaDao.updateMedia(updated)
            originalItem.id
        }
    }

    suspend fun prepareCropSourceUri(uriString: String): Uri = withContext(Dispatchers.IO) {
        if (uriString.startsWith("file://")) {
            val path = Uri.parse(uriString).path
            if (path != null && File(path).exists()) {
                return@withContext Uri.fromFile(File(path))
            }
        }
        if (uriString.startsWith("content://")) {
            try {
                val contentUri = Uri.parse(uriString)
                val tempFile = File(context.cacheDir, "crop_input_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(contentUri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempFile.exists() && tempFile.length() > 0) {
                    return@withContext Uri.fromFile(tempFile)
                }
            } catch (_: Exception) {}
            return@withContext Uri.parse(uriString)
        }
        // Remote or generated bitmap
        val cacheFile = File(context.cacheDir, "crop_input_${System.currentTimeMillis()}.jpg")
        try {
            val bitmap = loadBitmapFromUri(uriString) ?: Bitmap.createBitmap(1280, 800, Bitmap.Config.ARGB_8888).also {
                val c = android.graphics.Canvas(it)
                c.drawColor(android.graphics.Color.DKGRAY)
            }
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            Uri.fromFile(cacheFile)
        } catch (_: Exception) {
            Uri.fromFile(cacheFile)
        }
    }

    fun createCropDestinationUri(): Pair<File, Uri> {
        val cropDir = File(context.filesDir, "gallery_crops").apply { mkdirs() }
        val filename = "CROP_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(cropDir, filename)
        val uri = Uri.fromFile(file)
        return Pair(file, uri)
    }

    suspend fun saveCroppedPhoto(
        originalItem: MediaItem,
        destFile: File,
        saveAsNewCopy: Boolean = true
    ): MediaItem = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(destFile.absolutePath, options)
        val width = if (options.outWidth > 0) options.outWidth else originalItem.width
        val height = if (options.outHeight > 0) options.outHeight else originalItem.height
        val sizeBytes = destFile.length()
        val fileUriString = Uri.fromFile(destFile).toString()

        if (saveAsNewCopy) {
            val newItem = MediaItem(
                uri = fileUriString,
                name = destFile.name,
                mimeType = "image/jpeg",
                sizeBytes = sizeBytes,
                dateAdded = System.currentTimeMillis(),
                dateTaken = System.currentTimeMillis(),
                width = width,
                height = height,
                category = "Crops",
                albumId = originalItem.albumId,
                description = "Cropped copy of ${originalItem.name}"
            )
            val newId = mediaDao.insertMedia(newItem.withFormattedDate())
            newItem.copy(id = newId)
        } else {
            val updated = originalItem.copy(
                uri = fileUriString,
                name = destFile.name,
                sizeBytes = sizeBytes,
                width = width,
                height = height,
                category = "Crops"
            )
            mediaDao.updateMedia(updated.withFormattedDate())
            updated
        }
    }

    suspend fun saveCapturedPhoto(bitmap: Bitmap): Long = withContext(Dispatchers.IO) {
        val cameraDir = File(context.filesDir, "gallery_camera").apply { mkdirs() }
        val filename = "CAM_${System.currentTimeMillis()}.jpg"
        val destFile = File(cameraDir, filename)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val fileUri = Uri.fromFile(destFile).toString()
        val item = MediaItem(
            uri = fileUri,
            name = filename,
            mimeType = "image/jpeg",
            sizeBytes = destFile.length(),
            dateAdded = System.currentTimeMillis(),
            dateTaken = System.currentTimeMillis(),
            width = bitmap.width,
            height = bitmap.height,
            category = "Camera"
        )
        mediaDao.insertMedia(item.withFormattedDate())
    }

    fun createImageOutputFile(): Pair<File, Uri> {
        val cameraDir = File(context.filesDir, "gallery_camera").apply { mkdirs() }
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(cameraDir, filename)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    suspend fun saveCapturedPhotoFile(file: File, uri: Uri): Long = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        val width = if (options.outWidth > 0) options.outWidth else 1920
        val height = if (options.outHeight > 0) options.outHeight else 1080

        val item = MediaItem(
            uri = uri.toString(),
            name = file.name,
            mimeType = "image/jpeg",
            sizeBytes = file.length().coerceAtLeast(1024L),
            dateAdded = System.currentTimeMillis(),
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            category = "Camera"
        )
        mediaDao.insertMedia(item.withFormattedDate())
    }

    fun createVideoOutputFile(): Pair<File, Uri> {
        val cameraDir = File(context.filesDir, "gallery_camera").apply { mkdirs() }
        val filename = "VID_${System.currentTimeMillis()}.mp4"
        val file = File(cameraDir, filename)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(file, uri)
    }

    suspend fun saveCapturedVideo(file: File, uri: Uri): Long = withContext(Dispatchers.IO) {
        var durationMs = 0L
        var width = 1920
        var height = 1080
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val d = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
            if (d != null) durationMs = d
            if (w != null && w > 0) width = w
            if (h != null && h > 0) height = h
            retriever.release()
        } catch (_: Exception) {}

        val item = MediaItem(
            uri = uri.toString(),
            name = file.name,
            mimeType = "video/mp4",
            sizeBytes = file.length().coerceAtLeast(1024L),
            dateAdded = System.currentTimeMillis(),
            dateTaken = System.currentTimeMillis(),
            width = width,
            height = height,
            durationMs = durationMs,
            category = "Camera"
        )
        mediaDao.insertMedia(item.withFormattedDate())
    }

    suspend fun toggleFavorite(mediaItem: MediaItem) = withContext(Dispatchers.IO) {
        mediaDao.setFavorite(mediaItem.id, !mediaItem.isFavorite)
    }

    suspend fun batchSetFavorite(ids: List<Long>, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.batchSetFavorite(ids, isFavorite)
    }

    suspend fun moveToTrash(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.moveToTrash(ids)
    }

    suspend fun restoreFromTrash(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.restoreFromTrash(ids)
    }

    suspend fun permanentlyDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.deleteByIds(ids)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        mediaDao.emptyTrash()
    }

    suspend fun cleanExpiredTrash(): Int = withContext(Dispatchers.IO) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        mediaDao.deleteExpiredTrash(thirtyDaysAgo)
    }

    suspend fun moveToVault(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.setVaultStatus(ids, isVault = true)
    }

    suspend fun restoreFromVault(ids: List<Long>) = withContext(Dispatchers.IO) {
        mediaDao.setVaultStatus(ids, isVault = false)
    }

    suspend fun addMediaToAlbum(ids: List<Long>, albumId: Long) = withContext(Dispatchers.IO) {
        mediaDao.addToAlbum(ids, albumId)
    }

    suspend fun createAlbum(name: String, coverUri: String? = null): Long = withContext(Dispatchers.IO) {
        albumDao.insertAlbum(Album(name = name, coverUri = coverUri))
    }

    suspend fun renameAlbum(album: Album, newName: String) = withContext(Dispatchers.IO) {
        albumDao.updateAlbum(album.copy(name = newName))
    }

    suspend fun deleteAlbum(album: Album) = withContext(Dispatchers.IO) {
        albumDao.unlinkMediaFromAlbum(album.id)
        albumDao.deleteAlbum(album)
    }

    suspend fun loadBitmapFromUri(uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                // If network/placeholder URL, create an artistic high-res bitmap
                val url = java.net.URL(uriString)
                val conn = url.openConnection()
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.getInputStream().use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (_: Exception) {
            // Fallback generation for reliable on-device processing
            val w = 1280
            val h = 800
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = android.graphics.Canvas(bmp)
            c.drawColor(android.graphics.Color.rgb(30, 41, 59))
            val p = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 48f
            }
            c.drawText("Enhanced Studio Canvas", 100f, 400f, p)
            bmp
        }
    }

    suspend fun saveEnhancedPhoto(
        original: MediaItem,
        bitmap: Bitmap,
        titleSuffix: String = "AI_Enhanced"
    ): Long = withContext(Dispatchers.IO) {
        val enhancedDir = File(context.filesDir, "enhanced_photos").apply { mkdirs() }
        val filename = "${original.name.substringBeforeLast('.')}_${titleSuffix}_${System.currentTimeMillis()}.jpg"
        val destFile = File(enhancedDir, filename)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val fileUri = Uri.fromFile(destFile).toString()
        val newItem = MediaItem(
            uri = fileUri,
            name = filename,
            mimeType = "image/jpeg",
            sizeBytes = destFile.length(),
            dateAdded = System.currentTimeMillis(),
            dateTaken = System.currentTimeMillis(),
            width = bitmap.width,
            height = bitmap.height,
            category = "Edited Media",
            albumId = original.albumId,
            description = "AI Enhanced version of ${original.name}"
        )
        mediaDao.insertMedia(newItem.withFormattedDate())
    }

    suspend fun saveExtractedVideoFrame(originalVideo: MediaItem, bitmap: Bitmap): Long = withContext(Dispatchers.IO) {
        val framesDir = File(context.filesDir, "video_frames").apply { mkdirs() }
        val filename = "FRAME_${originalVideo.name.substringBeforeLast('.')}_${System.currentTimeMillis()}.jpg"
        val destFile = File(framesDir, filename)

        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val fileUri = Uri.fromFile(destFile).toString()
        val newItem = MediaItem(
            uri = fileUri,
            name = filename,
            mimeType = "image/jpeg",
            sizeBytes = destFile.length(),
            dateAdded = System.currentTimeMillis(),
            dateTaken = System.currentTimeMillis(),
            width = bitmap.width,
            height = bitmap.height,
            category = "Camera",
            description = "Extracted frame from ${originalVideo.name}"
        )
        mediaDao.insertMedia(newItem.withFormattedDate())
    }

    // Vault PIN security
    fun isVaultPinSet(): Boolean {
        return prefs.getString("vault_pin_hash", null) != null
    }

    fun setVaultPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString("vault_pin_hash", hash).apply()
    }

    fun verifyVaultPin(pin: String): Boolean {
        val savedHash = prefs.getString("vault_pin_hash", null) ?: return false
        return savedHash == hashPin(pin)
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // -------------------------------------------------------------
    // Full-Text Search (Room FTS4) Implementation
    // -------------------------------------------------------------

    enum class SearchType {
        ALL,
        NAME,
        DATE,
        TAGS
    }

    /**
     * Search media using Room Full-Text Search (FTS4) by name, date, or tags.
     */
    fun searchMedia(
        query: String,
        searchType: SearchType = SearchType.ALL
    ): Flow<List<MediaItem>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return mediaDao.getAllActiveMedia()
        }

        val ftsTokens = formatFtsQuery(trimmed)
        return when (searchType) {
            SearchType.NAME -> {
                if (ftsTokens.isNotBlank()) {
                    mediaDao.searchByNameFts(ftsTokens)
                } else {
                    mediaDao.searchMedia(trimmed)
                }
            }
            SearchType.DATE -> {
                if (ftsTokens.isNotBlank()) {
                    mediaDao.searchByDateFts(ftsTokens)
                } else {
                    mediaDao.searchMedia(trimmed)
                }
            }
            SearchType.TAGS -> {
                if (ftsTokens.isNotBlank()) {
                    mediaDao.searchByTagsFts(ftsTokens)
                } else {
                    mediaDao.searchMedia(trimmed)
                }
            }
            SearchType.ALL -> {
                if (ftsTokens.isNotBlank()) {
                    mediaDao.searchMediaFts(ftsTokens)
                } else {
                    mediaDao.searchMedia(trimmed)
                }
            }
        }
    }

    private fun formatFtsQuery(query: String): String {
        val tokens = query.trim()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(" ") { "$it*" }
    }
}

package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    // -------------------------------------------------------------
    // Active Media Queries
    // -------------------------------------------------------------

    @Query("SELECT * FROM media_items WHERE isTrash = 0 AND isVault = 0 ORDER BY dateTaken DESC")
    fun getAllActiveMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE uri = :uri LIMIT 1")
    suspend fun getMediaByUri(uri: String): MediaItem?

    @Query("SELECT * FROM media_items WHERE isTrash = 0 AND isVault = 0 AND category = :category ORDER BY dateTaken DESC")
    fun getMediaByCategory(category: String): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE isTrash = 0 AND isVault = 0")
    fun getActiveMediaCount(): Flow<Int>

    // -------------------------------------------------------------
    // Favorites Management
    // -------------------------------------------------------------

    @Query("SELECT * FROM media_items WHERE isTrash = 0 AND isVault = 0 AND isFavorite = 1 ORDER BY dateTaken DESC")
    fun getFavorites(): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE isTrash = 0 AND isVault = 0 AND isFavorite = 1")
    fun getFavoritesCount(): Flow<Int>

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id IN (:ids)")
    suspend fun batchSetFavorite(ids: List<Long>, isFavorite: Boolean)

    @Query("UPDATE media_items SET isFavorite = CASE WHEN isFavorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    // -------------------------------------------------------------
    // Trash Management
    // -------------------------------------------------------------

    @Query("SELECT * FROM media_items WHERE isTrash = 1 ORDER BY trashedAt DESC")
    fun getTrashMedia(): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE isTrash = 1")
    fun getTrashCount(): Flow<Int>

    @Query("UPDATE media_items SET isTrash = 1, trashedAt = :trashedAt WHERE id IN (:ids)")
    suspend fun moveToTrash(ids: List<Long>, trashedAt: Long = System.currentTimeMillis())

    @Query("UPDATE media_items SET isTrash = 0, trashedAt = NULL WHERE id IN (:ids)")
    suspend fun restoreFromTrash(ids: List<Long>)

    @Query("DELETE FROM media_items WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM media_items WHERE isTrash = 1 AND trashedAt IS NOT NULL AND trashedAt < :expirationTimestamp")
    suspend fun deleteExpiredTrash(expirationTimestamp: Long): Int

    // -------------------------------------------------------------
    // Albums Association
    // -------------------------------------------------------------

    @Query("SELECT * FROM media_items WHERE isTrash = 0 AND isVault = 0 AND albumId = :albumId ORDER BY dateTaken DESC")
    fun getMediaByAlbum(albumId: Long): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE isTrash = 0 AND isVault = 0 AND albumId = :albumId")
    fun getMediaCountByAlbum(albumId: Long): Flow<Int>

    @Query("UPDATE media_items SET albumId = :albumId WHERE id IN (:ids)")
    suspend fun addToAlbum(ids: List<Long>, albumId: Long?)

    @Query("UPDATE media_items SET albumId = NULL WHERE id IN (:ids)")
    suspend fun removeFromAlbum(ids: List<Long>)

    // -------------------------------------------------------------
    // Vault / Secure Storage
    // -------------------------------------------------------------

    @Query("SELECT * FROM media_items WHERE isVault = 1 AND isTrash = 0 ORDER BY dateTaken DESC")
    fun getVaultMedia(): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items WHERE isVault = 1 AND isTrash = 0")
    fun getVaultCount(): Flow<Int>

    @Query("UPDATE media_items SET isVault = :isVault, albumId = NULL WHERE id IN (:ids)")
    suspend fun setVaultStatus(ids: List<Long>, isVault: Boolean)

    // -------------------------------------------------------------
    // Full-Text Search (FTS4) & Filtering
    // -------------------------------------------------------------

    @Query("""
        SELECT media_items.* 
        FROM media_items 
        JOIN media_items_fts ON media_items.id = media_items_fts.rowid 
        WHERE media_items_fts MATCH :ftsQuery 
          AND media_items.isTrash = 0 
          AND media_items.isVault = 0 
        ORDER BY media_items.dateTaken DESC
    """)
    fun searchMediaFts(ftsQuery: String): Flow<List<MediaItem>>

    @Query("""
        SELECT media_items.* 
        FROM media_items 
        JOIN media_items_fts ON media_items.id = media_items_fts.rowid 
        WHERE media_items_fts MATCH 'name:' || :nameQuery 
          AND media_items.isTrash = 0 
          AND media_items.isVault = 0 
        ORDER BY media_items.dateTaken DESC
    """)
    fun searchByNameFts(nameQuery: String): Flow<List<MediaItem>>

    @Query("""
        SELECT media_items.* 
        FROM media_items 
        JOIN media_items_fts ON media_items.id = media_items_fts.rowid 
        WHERE media_items_fts MATCH 'dateText:' || :dateQuery 
          AND media_items.isTrash = 0 
          AND media_items.isVault = 0 
        ORDER BY media_items.dateTaken DESC
    """)
    fun searchByDateFts(dateQuery: String): Flow<List<MediaItem>>

    @Query("""
        SELECT media_items.* 
        FROM media_items 
        JOIN media_items_fts ON media_items.id = media_items_fts.rowid 
        WHERE media_items_fts MATCH 'tags:' || :tagQuery 
          AND media_items.isTrash = 0 
          AND media_items.isVault = 0 
        ORDER BY media_items.dateTaken DESC
    """)
    fun searchByTagsFts(tagQuery: String): Flow<List<MediaItem>>

    @Query("""
        SELECT * FROM media_items 
        WHERE isTrash = 0 AND isVault = 0 
          AND (name LIKE '%' || :query || '%' 
               OR description LIKE '%' || :query || '%' 
               OR tags LIKE '%' || :query || '%' 
               OR dateText LIKE '%' || :query || '%') 
        ORDER BY dateTaken DESC
    """)
    fun searchMedia(query: String): Flow<List<MediaItem>>

    // -------------------------------------------------------------
    // Insertion, Updates & Deletion
    // -------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>): List<Long>

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

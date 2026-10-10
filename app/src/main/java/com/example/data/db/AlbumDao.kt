package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Album
import com.example.data.model.AlbumWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY isSystem DESC, createdAt ASC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("""
        SELECT a.id, a.name, a.coverUri, a.createdAt, a.isSystem,
               COUNT(m.id) as mediaCount
        FROM albums a
        LEFT JOIN media_items m ON a.id = m.albumId AND m.isTrash = 0 AND m.isVault = 0
        GROUP BY a.id
        ORDER BY a.isSystem DESC, a.createdAt ASC
    """)
    fun getAlbumsWithCount(): Flow<List<AlbumWithCount>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Long): Album?

    @Query("SELECT * FROM albums WHERE name = :name LIMIT 1")
    suspend fun getAlbumByName(name: String): Album?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(albums: List<Album>)

    @Update
    suspend fun updateAlbum(album: Album)

    @Delete
    suspend fun deleteAlbum(album: Album)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Long)

    @Query("UPDATE albums SET coverUri = :coverUri WHERE id = :albumId")
    suspend fun updateCover(albumId: Long, coverUri: String?)

    @Query("UPDATE media_items SET albumId = NULL WHERE albumId = :albumId")
    suspend fun unlinkMediaFromAlbum(albumId: Long)

    @Transaction
    suspend fun deleteAlbumAndUnlink(albumId: Long) {
        unlinkMediaFromAlbum(albumId)
        deleteAlbumById(albumId)
    }
}

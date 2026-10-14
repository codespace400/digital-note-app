package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND folderName = :folder ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folder: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isDeleted = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun restoreFromTrash(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :timestamp WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    // Folders
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun deleteFolder(name: String)

    @Query("UPDATE notes SET folderName = 'All' WHERE folderName = :folderName")
    suspend fun reassignFolderNotesToDefault(folderName: String)

    // Searches
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE id = :id")
    suspend fun deleteRecentSearch(id: Long)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0 AND isArchived = 0")
    fun getTotalActiveNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND folderName = :folder")
    fun getFolderNotesCount(folder: String): Flow<Int>
}

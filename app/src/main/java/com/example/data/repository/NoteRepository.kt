package com.example.data.repository

import com.example.data.local.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getActiveNotes(): Flow<List<NoteEntity>> = noteDao.getActiveNotes()

    fun getNotesByFolder(folder: String): Flow<List<NoteEntity>> {
        return if (folder == "All" || folder.isBlank()) {
            noteDao.getActiveNotes()
        } else {
            noteDao.getNotesByFolder(folder)
        }
    }

    fun getArchivedNotes(): Flow<List<NoteEntity>> = noteDao.getArchivedNotes()

    fun getDeletedNotes(): Flow<List<NoteEntity>> = noteDao.getDeletedNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun observeNoteById(id: Long): Flow<NoteEntity?> = noteDao.observeNoteById(id)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            noteDao.insertNote(note.copy(createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
        } else {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun moveToTrash(id: Long) = noteDao.moveToTrash(id)

    suspend fun restoreFromTrash(id: Long) = noteDao.restoreFromTrash(id)

    suspend fun deletePermanently(id: Long) = noteDao.deletePermanently(id)

    suspend fun emptyTrash() = noteDao.emptyTrash()

    suspend fun togglePin(id: Long, currentPinned: Boolean) {
        noteDao.setPinned(id, !currentPinned)
    }

    suspend fun toggleArchive(id: Long, currentArchived: Boolean) {
        noteDao.setArchived(id, !currentArchived)
    }

    // Folders
    fun getAllFolders(): Flow<List<FolderEntity>> = noteDao.getAllFolders()

    suspend fun addFolder(name: String, colorHex: String = "#FBBF24", iconName: String = "folder") {
        noteDao.insertFolder(FolderEntity(name = name.trim(), colorHex = colorHex, iconName = iconName))
    }

    suspend fun deleteFolder(name: String) {
        noteDao.reassignFolderNotesToDefault(name)
        noteDao.deleteFolder(name)
    }

    // Recent Searches
    fun getRecentSearches(): Flow<List<RecentSearchEntity>> = noteDao.getRecentSearches()

    suspend fun recordSearch(query: String) {
        if (query.isNotBlank()) {
            noteDao.insertRecentSearch(RecentSearchEntity(query = query.trim(), timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecentSearch(id: Long) = noteDao.deleteRecentSearch(id)

    suspend fun clearRecentSearches() = noteDao.clearRecentSearches()
}

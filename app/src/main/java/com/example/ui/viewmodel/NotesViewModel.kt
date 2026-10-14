package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RecentSearchEntity
import com.example.data.repository.NoteRepository
import com.example.ui.common.NoteColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SaveStatus {
    SAVED,
    SAVING,
    IDLE
}

data class EditorState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val colorHex: String = "#FEF9C3",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val folderName: String = "All",
    val tagsCsv: String = "",
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val isLoaded: Boolean = false
)

data class FolderWithCount(
    val folder: FolderEntity,
    val count: Int
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    private val prefs = application.getSharedPreferences("quicknote_prefs", Context.MODE_PRIVATE)

    // Current screen navigation
    val selectedFolderFilter = MutableStateFlow("All")

    // Search query
    val searchQuery = MutableStateFlow("")

    // Settings
    val defaultNoteColor = MutableStateFlow(prefs.getString("default_color", "#FEF9C3") ?: "#FEF9C3")
    val fontSizeOption = MutableStateFlow(prefs.getString("font_size", "Regular") ?: "Regular") // Compact, Regular, Large
    val syncEnabled = MutableStateFlow(prefs.getBoolean("sync_enabled", true))
    val lastBackupTimestamp = MutableStateFlow(prefs.getLong("last_backup", System.currentTimeMillis() - 86400000L))
    val isBackingUp = MutableStateFlow(false)

    // Active Editor State
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())
    }

    // Active Notes Stream filtered by selected folder
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeNotes: StateFlow<List<NoteEntity>> = selectedFolderFilter
        .flatMapLatest { folder -> repository.getNotesByFolder(folder) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active notes (for counting and general display)
    val allActiveNotes: StateFlow<List<NoteEntity>> = repository.getActiveNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trash notes
    val deletedNotes: StateFlow<List<NoteEntity>> = repository.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Folders
    val folders: StateFlow<List<FolderEntity>> = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Folders with count
    val foldersWithCount: StateFlow<List<FolderWithCount>> = combine(folders, allActiveNotes) { folderList, notes ->
        folderList.map { folder ->
            val count = notes.count { it.folderName.equals(folder.name, ignoreCase = true) }
            FolderWithCount(folder, count)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Searches
    val recentSearches: StateFlow<List<RecentSearchEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search Results
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<NoteEntity>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getActiveNotes()
            } else {
                repository.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter by folder
    fun selectFolder(folder: String) {
        selectedFolderFilter.value = folder
    }

    // Search query update
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun submitSearch(query: String) {
        if (query.isNotBlank()) {
            searchQuery.value = query
            viewModelScope.launch {
                repository.recordSearch(query)
            }
        }
    }

    fun deleteRecentSearch(id: Long) {
        viewModelScope.launch {
            repository.deleteRecentSearch(id)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    // Note actions
    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.togglePin(note.id, note.isPinned)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            repository.moveToTrash(noteId)
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deletePermanently(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    // Folders
    fun createFolder(name: String, colorHex: String = "#FBBF24") {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.addFolder(name, colorHex)
            }
        }
    }

    fun deleteFolder(name: String) {
        viewModelScope.launch {
            repository.deleteFolder(name)
            if (selectedFolderFilter.value == name) {
                selectedFolderFilter.value = "All"
            }
        }
    }

    // Editor Actions
    fun initNewNote(defaultFolder: String = "All") {
        val folder = if (defaultFolder == "All") "Personal" else defaultFolder
        _editorState.value = EditorState(
            noteId = 0L,
            title = "",
            content = "",
            colorHex = defaultNoteColor.value,
            isPinned = false,
            isArchived = false,
            folderName = folder,
            tagsCsv = "",
            saveStatus = SaveStatus.SAVED,
            isLoaded = true
        )
    }

    fun loadNoteForEditing(noteId: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                _editorState.value = EditorState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    colorHex = note.colorHex,
                    isPinned = note.isPinned,
                    isArchived = note.isArchived,
                    folderName = note.folderName,
                    tagsCsv = note.tagsCsv,
                    saveStatus = SaveStatus.SAVED,
                    isLoaded = true
                )
            } else {
                initNewNote()
            }
        }
    }

    fun onEditorTitleChanged(newTitle: String) {
        _editorState.update { it.copy(title = newTitle, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun onEditorContentChanged(newContent: String) {
        _editorState.update { it.copy(content = newContent, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun onEditorColorChanged(newHex: String) {
        _editorState.update { it.copy(colorHex = newHex, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun onEditorFolderChanged(newFolder: String) {
        _editorState.update { it.copy(folderName = newFolder, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun toggleEditorPin() {
        val current = _editorState.value.isPinned
        _editorState.update { it.copy(isPinned = !current, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun toggleEditorArchive() {
        val current = _editorState.value.isArchived
        _editorState.update { it.copy(isArchived = !current, saveStatus = SaveStatus.SAVING) }
        scheduleAutoSave()
    }

    fun deleteCurrentEditorNote() {
        val currentId = _editorState.value.noteId
        if (currentId != 0L) {
            viewModelScope.launch {
                repository.moveToTrash(currentId)
            }
        }
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500)
            flushAutoSave()
        }
    }

    fun flushAutoSave() {
        val current = _editorState.value
        // Only save if note has content or title or is already persisted
        if (current.title.isNotBlank() || current.content.isNotBlank() || current.noteId != 0L) {
            viewModelScope.launch {
                val entity = NoteEntity(
                    id = current.noteId,
                    title = current.title,
                    content = current.content,
                    colorHex = current.colorHex,
                    isPinned = current.isPinned,
                    isArchived = current.isArchived,
                    isDeleted = false,
                    folderName = current.folderName,
                    tagsCsv = current.tagsCsv,
                    updatedAt = System.currentTimeMillis()
                )
                val newId = repository.saveNote(entity)
                if (current.noteId == 0L) {
                    _editorState.update { it.copy(noteId = newId, saveStatus = SaveStatus.SAVED) }
                } else {
                    _editorState.update { it.copy(saveStatus = SaveStatus.SAVED) }
                }
            }
        } else {
            _editorState.update { it.copy(saveStatus = SaveStatus.SAVED) }
        }
    }

    // Settings actions
    fun setDefaultColor(colorHex: String) {
        defaultNoteColor.value = colorHex
        prefs.edit().putString("default_color", colorHex).apply()
    }

    fun setFontSize(option: String) {
        fontSizeOption.value = option
        prefs.edit().putString("font_size", option).apply()
    }

    fun toggleSync(enabled: Boolean) {
        syncEnabled.value = enabled
        prefs.edit().putBoolean("sync_enabled", enabled).apply()
    }

    fun triggerBackup() {
        viewModelScope.launch {
            isBackingUp.value = true
            delay(1200) // Friendly backup simulation
            val now = System.currentTimeMillis()
            lastBackupTimestamp.value = now
            prefs.edit().putLong("last_backup", now).apply()
            isBackingUp.value = false
        }
    }
}

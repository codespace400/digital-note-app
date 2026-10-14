package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read app_name string resource matches QuickNote`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickNote", appName)
    }

    @Test
    fun `insert note and query active notes`() = runBlocking {
        val note = NoteEntity(
            title = "Meeting Notes",
            content = "Discuss Q4 product goals and roadmap.",
            colorHex = "#D1FAE5",
            folderName = "Work"
        )
        val id = noteDao.insertNote(note)
        assertTrue(id > 0)

        val activeNotes = noteDao.getActiveNotes().first()
        assertEquals(1, activeNotes.size)
        assertEquals("Meeting Notes", activeNotes[0].title)
        assertEquals("#D1FAE5", activeNotes[0].colorHex)
    }

    @Test
    fun `search notes matches title or content`() = runBlocking {
        noteDao.insertNote(NoteEntity(title = "Grocery list", content = "Avocados, bananas"))
        noteDao.insertNote(NoteEntity(title = "Ideas", content = "Build a notes app with pastel cards"))

        val searchBanana = noteDao.searchNotes("bananas").first()
        assertEquals(1, searchBanana.size)
        assertEquals("Grocery list", searchBanana[0].title)

        val searchPastel = noteDao.searchNotes("pastel").first()
        assertEquals(1, searchPastel.size)
        assertEquals("Ideas", searchPastel[0].title)
    }

    @Test
    fun `trash and restore flow operates correctly`() = runBlocking {
        val id = noteDao.insertNote(NoteEntity(title = "Temporary Idea", content = "Draft"))
        val activeBefore = noteDao.getActiveNotes().first()
        assertEquals(1, activeBefore.size)

        // Move to trash
        noteDao.moveToTrash(id)
        val activeAfterTrash = noteDao.getActiveNotes().first()
        assertEquals(0, activeAfterTrash.size)

        val deletedNotes = noteDao.getDeletedNotes().first()
        assertEquals(1, deletedNotes.size)

        // Restore
        noteDao.restoreFromTrash(id)
        val activeAfterRestore = noteDao.getActiveNotes().first()
        assertEquals(1, activeAfterRestore.size)
    }

    @Test
    fun `folders and pin toggles work as expected`() = runBlocking {
        noteDao.insertFolder(FolderEntity(name = "Personal", colorHex = "#10B981"))
        val folders = noteDao.getAllFolders().first()
        assertEquals(1, folders.size)

        val id = noteDao.insertNote(NoteEntity(title = "Pinned Task", isPinned = false))
        noteDao.setPinned(id, true)
        val fetched = noteDao.getNoteById(id)
        assertNotNull(fetched)
        assertTrue(fetched!!.isPinned)
    }
}

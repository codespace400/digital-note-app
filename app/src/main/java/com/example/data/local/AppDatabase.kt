package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.model.RecentSearchEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [NoteEntity::class, FolderEntity::class, RecentSearchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quicknote_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial data
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = INSTANCE?.noteDao() ?: return@launch
                    val now = System.currentTimeMillis()

                    // Seed default folders
                    dao.insertFolder(FolderEntity(name = "Work", colorHex = "#3B82F6", iconName = "work", createdAt = now))
                    dao.insertFolder(FolderEntity(name = "Personal", colorHex = "#10B981", iconName = "person", createdAt = now))
                    dao.insertFolder(FolderEntity(name = "Ideas", colorHex = "#F59E0B", iconName = "lightbulb", createdAt = now))
                    dao.insertFolder(FolderEntity(name = "Todo", colorHex = "#8B5CF6", iconName = "checklist", createdAt = now))

                    // Seed starter notes
                    dao.insertNote(
                        NoteEntity(
                            title = "Welcome to QuickNote! 📝",
                            content = """QuickNote is your warm, fluid space for ideas, thoughts, and tasks.

Features to explore:
• Tap any note to edit or format
• Tap the palette to switch pastel colors
• Filter notes by custom folders and tags
• Pin crucial notes to the top
• Quick search with live preview

- [x] Download QuickNote
- [ ] Create my first new note
- [ ] Try formatting with bold, italic, and checklists
- [ ] Organize with custom folders""".trimIndent(),
                            colorHex = "#FEF9C3", // Butter
                            isPinned = true,
                            folderName = "Personal",
                            tagsCsv = "Welcome,Guide",
                            createdAt = now,
                            updatedAt = now
                        )
                    )

                    dao.insertNote(
                        NoteEntity(
                            title = "Weekly Groceries 🥑",
                            content = """Fresh ingredients for dinner:
- [x] Hass Avocados & limes
- [x] Sourdough bread
- [ ] Greek yogurt & wild honey
- [ ] Oat milk & cold brew
- [ ] Cherry tomatoes & basil
- [ ] Dark roast espresso beans""".trimIndent(),
                            colorHex = "#D1FAE5", // Mint
                            isPinned = false,
                            folderName = "Personal",
                            tagsCsv = "Food,Shopping",
                            createdAt = now - 3600000L,
                            updatedAt = now - 3600000L
                        )
                    )

                    dao.insertNote(
                        NoteEntity(
                            title = "Mobile App Architecture 💡",
                            content = """Key guidelines for modern Compose apps:
• Clean Architecture with MVVM + Room
• Staggered masonry layout for dynamic heights
• Delightful pastel palette with warm background (#FFFDF7)
• Responsive 48dp touch targets for accessibility
• Offline-first with instant reactivity""".trimIndent(),
                            colorHex = "#E0F2FE", // Sky Blue
                            isPinned = true,
                            folderName = "Work",
                            tagsCsv = "Tech,Android",
                            createdAt = now - 7200000L,
                            updatedAt = now - 7200000L
                        )
                    )

                    dao.insertNote(
                        NoteEntity(
                            title = "Weekend Getaway Itinerary ✈️",
                            content = """Cozy mountain cabin itinerary:
• Friday evening: Arrival, fireplace & hot cocoa
• Saturday morning: Pine ridge scenic trail hike
• Saturday afternoon: Artisan market & bakery
• Sunday morning: Lakeside sunrise coffee""".trimIndent(),
                            colorHex = "#FFEDD5", // Peach
                            isPinned = false,
                            folderName = "Ideas",
                            tagsCsv = "Travel,Weekend",
                            createdAt = now - 14400000L,
                            updatedAt = now - 14400000L
                        )
                    )

                    dao.insertNote(
                        NoteEntity(
                            title = "Reading Wishlist 2026 📚",
                            content = """Books on design, psychology, and technology:
- [x] The Design of Everyday Things
- [ ] Thinking, Fast and Slow
- [ ] Atomic Habits
- [ ] Clean Code Architecture
- [ ] Creative Selection by Ken Kocienda""".trimIndent(),
                            colorHex = "#EDE9FE", // Lavender
                            isPinned = false,
                            folderName = "Ideas",
                            tagsCsv = "Books,Growth",
                            createdAt = now - 28800000L,
                            updatedAt = now - 28800000L
                        )
                    )

                    // Seed sample recent search
                    dao.insertRecentSearch(RecentSearchEntity(query = "Groceries", timestamp = now))
                    dao.insertRecentSearch(RecentSearchEntity(query = "Architecture", timestamp = now - 1000L))
                }
            }
        }
    }
}

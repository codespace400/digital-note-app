package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.NoteCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    onNavigateToEditor: (Long?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeNotes by viewModel.activeNotes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolderFilter.collectAsState()
    val deletedNotes by viewModel.deletedNotes.collectAsState()

    val pinnedNotes = activeNotes.filter { it.isPinned }
    val otherNotes = activeNotes.filter { !it.isPinned }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = null,
                                tint = CharcoalText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "QuickNote",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToTrash,
                        modifier = Modifier.testTag("home_trash_button")
                    ) {
                        if (deletedNotes.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ) {
                                        Text("${deletedNotes.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Trash",
                                    tint = CharcoalText.copy(alpha = 0.8f)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Trash",
                                tint = CharcoalText.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWarm
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = AmberAccent,
                contentColor = CharcoalText,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("fab_add_note")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create new note",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar header button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToSearch() }
                    .testTag("home_search_bar"),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search notes, checklists, tags...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Folder Filter Chips
            FolderChipsRow(
                folders = folders,
                selectedFolder = selectedFolder,
                onFolderSelected = { viewModel.selectFolder(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Notes Content
            if (activeNotes.isEmpty()) {
                EmptyNotesState(
                    selectedFolder = selectedFolder,
                    onCreateNote = { onNavigateToEditor(null) }
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notes_staggered_grid"),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    // Pinned Notes section
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PINNED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNavigateToEditor(note.id) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }

                    // Others section
                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = "OTHERS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )
                            }
                        }

                        items(otherNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNavigateToEditor(note.id) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderChipsRow(
    folders: List<FolderEntity>,
    selectedFolder: String,
    onFolderSelected: (String) -> Unit
) {
    val allOptions = listOf("All") + folders.map { it.name }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("folder_filter_row"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allOptions) { folder ->
            val isSelected = folder.equals(selectedFolder, ignoreCase = true)

            FilterChip(
                selected = isSelected,
                onClick = { onFolderSelected(folder) },
                label = {
                    Text(
                        text = folder,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                leadingIcon = if (folder != "All") {
                    {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberAccent,
                    selectedLabelColor = CharcoalText,
                    selectedLeadingIconColor = CharcoalText,
                    containerColor = Color.White,
                    labelColor = CharcoalText.copy(alpha = 0.8f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) AmberAccent else BorderSubtle,
                    selectedBorderColor = AmberAccent,
                    borderWidth = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun EmptyNotesState(
    selectedFolder: String,
    onCreateNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (selectedFolder == "All") "No notes yet" else "No notes in '$selectedFolder'",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Capture thoughts, ideas, and checklists instantly.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onCreateNote() }
                    .testTag("empty_state_create_button"),
                color = AmberAccent,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = CharcoalText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Note",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CharcoalText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

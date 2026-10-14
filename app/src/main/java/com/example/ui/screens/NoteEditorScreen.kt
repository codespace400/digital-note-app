package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.NoteColors
import com.example.ui.components.FormatAction
import com.example.ui.components.FormattingToolbar
import com.example.ui.components.NoteColorPicker
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EditorState
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.SaveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editorState by viewModel.editorState.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val fontSizeOption by viewModel.fontSizeOption.collectAsState()

    var isColorPickerVisible by remember { mutableStateOf(false) }
    var isFolderDropdownExpanded by remember { mutableStateOf(false) }

    // Text field state with cursor tracking
    var titleTextFieldValue by remember { mutableStateOf(TextFieldValue(editorState.title)) }
    var contentTextFieldValue by remember { mutableStateOf(TextFieldValue(editorState.content)) }

    // Initialize or load note
    LaunchedEffect(noteId) {
        if (noteId == null || noteId == 0L) {
            viewModel.initNewNote()
        } else {
            viewModel.loadNoteForEditing(noteId)
        }
    }

    // Sync state when loaded
    LaunchedEffect(editorState.isLoaded) {
        if (editorState.isLoaded) {
            titleTextFieldValue = TextFieldValue(editorState.title, TextRange(editorState.title.length))
            contentTextFieldValue = TextFieldValue(editorState.content, TextRange(editorState.content.length))
        }
    }

    // Handle back press safely
    BackHandler {
        viewModel.flushAutoSave()
        onNavigateBack()
    }

    val pastelTheme = NoteColors.getTheme(editorState.colorHex)
    val animatedBg by animateColorAsState(
        targetValue = pastelTheme.backgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "bg_color"
    )

    // Font size scaling
    val bodyFontSize = when (fontSizeOption) {
        "Compact" -> 14.sp
        "Large" -> 18.sp
        else -> 16.sp
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = animatedBg,
        topBar = {
            TopAppBar(
                title = {
                    // Auto-save status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("editor_auto_save_status")
                    ) {
                        when (editorState.saveStatus) {
                            SaveStatus.SAVING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFD97706)
                                )
                                Text(
                                    text = "Saving...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            SaveStatus.SAVED -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Saved",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Saved",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF059669),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            SaveStatus.IDLE -> {}
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.flushAutoSave()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CharcoalText
                        )
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(
                        onClick = { viewModel.toggleEditorPin() },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (editorState.isPinned) "Unpin" else "Pin",
                            tint = if (editorState.isPinned) Color(0xFFD97706) else CharcoalText.copy(alpha = 0.7f)
                        )
                    }

                    // Archive toggle
                    IconButton(
                        onClick = { viewModel.toggleEditorArchive() },
                        modifier = Modifier.testTag("editor_archive_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.isArchived) Icons.Filled.Archive else Icons.Outlined.Archive,
                            contentDescription = if (editorState.isArchived) "Unarchive" else "Archive",
                            tint = if (editorState.isArchived) Color(0xFFD97706) else CharcoalText.copy(alpha = 0.7f)
                        )
                    }

                    // Delete to Trash
                    IconButton(
                        onClick = {
                            viewModel.deleteCurrentEditorNote()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("editor_trash_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Move to trash",
                            tint = CharcoalText.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Expandable color picker
                AnimatedVisibility(
                    visible = isColorPickerVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        NoteColorPicker(
                            selectedHex = editorState.colorHex,
                            onColorSelected = { hex ->
                                viewModel.onEditorColorChanged(hex)
                            },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Formatting Toolbar
                FormattingToolbar(
                    isColorPickerOpen = isColorPickerVisible,
                    onAction = { action ->
                        when (action) {
                            FormatAction.COLOR_TOGGLE -> {
                                isColorPickerVisible = !isColorPickerVisible
                            }
                            FormatAction.BOLD -> {
                                contentTextFieldValue = applyWrapFormat(contentTextFieldValue, "**")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                            FormatAction.ITALIC -> {
                                contentTextFieldValue = applyWrapFormat(contentTextFieldValue, "*")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                            FormatAction.STRIKETHROUGH -> {
                                contentTextFieldValue = applyWrapFormat(contentTextFieldValue, "~~")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                            FormatAction.BULLET -> {
                                contentTextFieldValue = insertPrefix(contentTextFieldValue, "• ")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                            FormatAction.CHECKLIST -> {
                                contentTextFieldValue = insertPrefix(contentTextFieldValue, "- [ ] ")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                            FormatAction.HEADING -> {
                                contentTextFieldValue = insertPrefix(contentTextFieldValue, "# ")
                                viewModel.onEditorContentChanged(contentTextFieldValue.text)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Folder selector chip
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.65f))
                        .clickable { isFolderDropdownExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("editor_folder_selector")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = pastelTheme.accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (editorState.folderName.isBlank()) "No Folder" else editorState.folderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "▾",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                DropdownMenu(
                    expanded = isFolderDropdownExpanded,
                    onDismissRequest = { isFolderDropdownExpanded = false }
                ) {
                    val allFolderOptions = listOf("All") + folders.map { it.name }
                    allFolderOptions.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder) },
                            onClick = {
                                viewModel.onEditorFolderChanged(folder)
                                isFolderDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title Input
            BasicTextField(
                value = titleTextFieldValue,
                onValueChange = {
                    titleTextFieldValue = it
                    viewModel.onEditorTitleChanged(it.text)
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    fontSize = 24.sp
                ),
                cursorBrush = SolidColor(CharcoalText),
                decorationBox = { innerTextField ->
                    if (titleTextFieldValue.text.isEmpty()) {
                        Text(
                            text = "Title",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText.copy(alpha = 0.35f),
                                fontSize = 24.sp
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Input
            BasicTextField(
                value = contentTextFieldValue,
                onValueChange = {
                    contentTextFieldValue = it
                    viewModel.onEditorContentChanged(it.text)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = CharcoalText.copy(alpha = 0.9f),
                    fontSize = bodyFontSize,
                    lineHeight = (bodyFontSize.value * 1.5).sp
                ),
                cursorBrush = SolidColor(CharcoalText),
                decorationBox = { innerTextField ->
                    if (contentTextFieldValue.text.isEmpty()) {
                        Text(
                            text = "Type your thoughts, tasks, ideas...\n\nUse the toolbar below for checklists, bullet points, and formatting.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = CharcoalText.copy(alpha = 0.35f),
                                fontSize = bodyFontSize,
                                lineHeight = (bodyFontSize.value * 1.5).sp
                            )
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .testTag("editor_content_input")
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// Helpers for inserting markdown/prefix
private fun applyWrapFormat(current: TextFieldValue, delimiter: String): TextFieldValue {
    val selection = current.selection
    val text = current.text

    return if (selection.collapsed) {
        val newText = text.substring(0, selection.start) + delimiter + delimiter + text.substring(selection.end)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + delimiter.length)
        )
    } else {
        val selectedText = text.substring(selection.start, selection.end)
        val newText = text.substring(0, selection.start) + delimiter + selectedText + delimiter + text.substring(selection.end)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + delimiter.length, selection.end + delimiter.length)
        )
    }
}

private fun insertPrefix(current: TextFieldValue, prefix: String): TextFieldValue {
    val selection = current.selection
    val text = current.text

    val insertion = if (selection.start > 0 && text[selection.start - 1] != '\n') {
        "\n$prefix"
    } else {
        prefix
    }

    val newText = text.substring(0, selection.start) + insertion + text.substring(selection.end)
    return TextFieldValue(
        text = newText,
        selection = TextRange(selection.start + insertion.length)
    )
}

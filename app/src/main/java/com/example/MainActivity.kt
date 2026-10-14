package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.QuickNoteBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.BackgroundWarm
import com.example.ui.theme.QuickNoteTheme
import com.example.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickNoteTheme {
                QuickNoteApp()
            }
        }
    }
}

@Composable
fun QuickNoteApp() {
    val viewModel: NotesViewModel = viewModel()

    // Navigation backstack management
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (screen is Screen.Home) {
            backStack.clear()
            backStack.add(Screen.Home)
        } else if (currentScreen != screen) {
            backStack.add(screen)
        }
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // Handle back button for sub-screens
    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    val isEditor = currentScreen is Screen.Editor
    val isTrash = currentScreen is Screen.Trash

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundWarm,
        bottomBar = {
            if (!isEditor && !isTrash) {
                QuickNoteBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { targetScreen ->
                        navigateTo(targetScreen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (!isEditor && !isTrash) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToEditor = { noteId ->
                                navigateTo(Screen.Editor(noteId))
                            },
                            onNavigateToSearch = {
                                navigateTo(Screen.Search)
                            },
                            onNavigateToTrash = {
                                navigateTo(Screen.Trash)
                            }
                        )
                    }

                    is Screen.Editor -> {
                        NoteEditorScreen(
                            noteId = screen.noteId,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navigateBack()
                            }
                        )
                    }

                    is Screen.Search -> {
                        SearchScreen(
                            viewModel = viewModel,
                            onNavigateToEditor = { noteId ->
                                navigateTo(Screen.Editor(noteId))
                            },
                            onNavigateBack = {
                                navigateBack()
                            }
                        )
                    }

                    is Screen.Folders -> {
                        FoldersScreen(
                            viewModel = viewModel,
                            onSelectFolderAndGoHome = { folderName ->
                                viewModel.selectFolder(folderName)
                                navigateTo(Screen.Home)
                            }
                        )
                    }

                    is Screen.Trash -> {
                        TrashScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navigateBack()
                            }
                        )
                    }

                    is Screen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

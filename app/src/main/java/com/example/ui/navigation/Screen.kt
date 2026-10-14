package com.example.ui.navigation

sealed class Screen {
    data object Home : Screen()
    data class Editor(val noteId: Long? = null) : Screen()
    data object Search : Screen()
    data object Folders : Screen()
    data object Trash : Screen()
    data object Settings : Screen()
}

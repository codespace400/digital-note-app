package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.TextMuted

sealed class NavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Notes : NavItem(
        screen = Screen.Home,
        title = "Notes",
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description,
        testTag = "nav_notes"
    )

    data object Folders : NavItem(
        screen = Screen.Folders,
        title = "Folders",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder,
        testTag = "nav_folders"
    )

    data object Search : NavItem(
        screen = Screen.Search,
        title = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        testTag = "nav_search"
    )

    data object Settings : NavItem(
        screen = Screen.Settings,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
}

@Composable
fun QuickNoteBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Notes,
        NavItem.Folders,
        NavItem.Search,
        NavItem.Settings
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFFFFFDF7),
        tonalElevation = 2.dp
    ) {
        items.forEach { item ->
            val isSelected = when (item) {
                NavItem.Notes -> currentScreen is Screen.Home
                NavItem.Folders -> currentScreen is Screen.Folders
                NavItem.Search -> currentScreen is Screen.Search
                NavItem.Settings -> currentScreen is Screen.Settings
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        )
                    )
                },
                modifier = Modifier.testTag(item.testTag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF78350F),
                    selectedTextColor = CharcoalText,
                    indicatorColor = AmberAccent.copy(alpha = 0.5f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

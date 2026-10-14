package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalText

enum class FormatAction {
    BOLD,
    ITALIC,
    BULLET,
    CHECKLIST,
    HEADING,
    STRIKETHROUGH,
    COLOR_TOGGLE
}

@Composable
fun FormattingToolbar(
    onAction: (FormatAction) -> Unit,
    isColorPickerOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 3.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarButton(
                icon = Icons.Filled.FormatBold,
                description = "Bold",
                testTag = "format_bold",
                onClick = { onAction(FormatAction.BOLD) }
            )

            ToolbarButton(
                icon = Icons.Filled.FormatItalic,
                description = "Italic",
                testTag = "format_italic",
                onClick = { onAction(FormatAction.ITALIC) }
            )

            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                description = "Bullet list",
                testTag = "format_bullet",
                onClick = { onAction(FormatAction.BULLET) }
            )

            ToolbarButton(
                icon = Icons.Filled.CheckBox,
                description = "Checklist item",
                testTag = "format_checklist",
                onClick = { onAction(FormatAction.CHECKLIST) }
            )

            ToolbarButton(
                icon = Icons.Filled.FormatSize,
                description = "Heading",
                testTag = "format_heading",
                onClick = { onAction(FormatAction.HEADING) }
            )

            ToolbarButton(
                icon = Icons.Filled.FormatStrikethrough,
                description = "Strikethrough",
                testTag = "format_strikethrough",
                onClick = { onAction(FormatAction.STRIKETHROUGH) }
            )

            ToolbarButton(
                icon = Icons.Filled.Palette,
                description = "Note Color",
                testTag = "format_palette",
                isSelected = isColorPickerOpen,
                onClick = { onAction(FormatAction.COLOR_TOGGLE) }
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    description: String,
    testTag: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .testTag(testTag)
            .then(
                if (isSelected) {
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberAccent.copy(alpha = 0.35f))
                } else Modifier
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) Color(0xFFB45309) else CharcoalText,
            modifier = Modifier.size(22.dp)
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.NoteColors
import com.example.ui.common.PastelTheme
import com.example.ui.theme.CharcoalText

@Composable
fun NoteColorPicker(
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Note Color",
            style = MaterialTheme.typography.labelMedium.copy(
                color = CharcoalText.copy(alpha = 0.7f),
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(NoteColors.options) { theme ->
                val isSelected = theme.hex.equals(selectedHex, ignoreCase = true)
                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) CharcoalText else theme.borderColor,
                    animationSpec = tween(durationMillis = 200),
                    label = "border_color"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(theme.backgroundColor)
                        .border(
                            BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.2.dp,
                                color = animatedBorderColor
                            ),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(theme.hex) }
                        .testTag("color_picker_${theme.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "${theme.name} selected",
                            tint = CharcoalText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

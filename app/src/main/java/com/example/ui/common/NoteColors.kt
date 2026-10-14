package com.example.ui.common

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.PastelButter
import com.example.ui.theme.PastelDefault
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelSky

data class PastelTheme(
    val name: String,
    val hex: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val accentColor: Color
)

object NoteColors {
    val options = listOf(
        PastelTheme("Butter", "#FEF9C3", PastelButter, Color(0xFFFDE047), Color(0xFFCA8A04)),
        PastelTheme("Mint", "#D1FAE5", PastelMint, Color(0xFFA7F3D0), Color(0xFF059669)),
        PastelTheme("Lavender", "#EDE9FE", PastelLavender, Color(0xFFDDD6FE), Color(0xFF7C3AED)),
        PastelTheme("Peach", "#FFEDD5", PastelPeach, Color(0xFFFED7AA), Color(0xFFEA580C)),
        PastelTheme("Sky Blue", "#E0F2FE", PastelSky, Color(0xFFBAE6FD), Color(0xFF0284C7)),
        PastelTheme("Rose", "#FFE4E6", PastelRose, Color(0xFFFECDD3), Color(0xFFE11D48)),
        PastelTheme("White", "#FFFFFF", PastelDefault, BorderSubtle, Color(0xFF64748B))
    )

    fun getTheme(hex: String?): PastelTheme {
        return options.find { it.hex.equals(hex, ignoreCase = true) } ?: options[0]
    }
}

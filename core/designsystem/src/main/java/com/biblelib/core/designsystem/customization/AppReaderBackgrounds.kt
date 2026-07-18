package com.biblelib.core.designsystem.customization

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** A selectable background for the Reader screen. */
data class ReaderBackgroundOption(
    val id: String,
    val displayName: String,
    val swatch: Color,
    /** Resolves the actual brush to paint behind reader content; null = follow the app theme. */
    val brush: @Composable () -> Brush,
)

object AppReaderBackgrounds {

    val DEFAULT_ID = "default"

    val ALL: List<ReaderBackgroundOption> = listOf(
        ReaderBackgroundOption(
            id = "default",
            displayName = "Default",
            swatch = Color(0xFFFFFFFF),
            brush = { Brush.verticalGradient(listOf(themeSurface(), themeSurface())) },
        ),
        ReaderBackgroundOption(
            id = "cream",
            displayName = "Cream",
            swatch = Color(0xFFF7EFDD),
            brush = { Brush.verticalGradient(listOf(Color(0xFFFAF3E4), Color(0xFFF3E9D2))) },
        ),
        ReaderBackgroundOption(
            id = "sepia",
            displayName = "Sepia",
            swatch = Color(0xFFEADDC7),
            brush = { Brush.verticalGradient(listOf(Color(0xFFEFE3CC), Color(0xFFE4D3AF))) },
        ),
        ReaderBackgroundOption(
            id = "parchment",
            displayName = "Parchment",
            swatch = Color(0xFFEFE6D2),
            brush = { Brush.linearGradient(listOf(Color(0xFFF3ECDA), Color(0xFFE8DBB8))) },
        ),
        ReaderBackgroundOption(
            id = "mint",
            displayName = "Mint",
            swatch = Color(0xFFE3F1EA),
            brush = { Brush.verticalGradient(listOf(Color(0xFFEAF6F0), Color(0xFFDCEEE3))) },
        ),
        ReaderBackgroundOption(
            id = "sky",
            displayName = "Sky",
            swatch = Color(0xFFE2EEF7),
            brush = { Brush.verticalGradient(listOf(Color(0xFFEAF3FA), Color(0xFFD9E9F5))) },
        ),
        ReaderBackgroundOption(
            id = "blush",
            displayName = "Blush",
            swatch = Color(0xFFF7E4E0),
            brush = { Brush.verticalGradient(listOf(Color(0xFFFBEDE9), Color(0xFFF4DAD3))) },
        ),
        ReaderBackgroundOption(
            id = "night",
            displayName = "Night",
            swatch = Color(0xFF15181C),
            brush = { Brush.verticalGradient(listOf(Color(0xFF1B1E23), Color(0xFF0F1114))) },
        ),
        ReaderBackgroundOption(
            id = "charcoal",
            displayName = "Charcoal",
            swatch = Color(0xFF262524),
            brush = { Brush.verticalGradient(listOf(Color(0xFF2C2B29), Color(0xFF1C1B1A))) },
        ),
        ReaderBackgroundOption(
            id = "forest",
            displayName = "Forest",
            swatch = Color(0xFF16211B),
            brush = { Brush.verticalGradient(listOf(Color(0xFF1B2A22), Color(0xFF10190F))) },
        ),
    )

    fun byId(id: String): ReaderBackgroundOption = ALL.find { it.id == id } ?: ALL.first()

    @Composable
    @ReadOnlyComposable
    private fun themeSurface(): Color = MaterialTheme.colorScheme.surface
}

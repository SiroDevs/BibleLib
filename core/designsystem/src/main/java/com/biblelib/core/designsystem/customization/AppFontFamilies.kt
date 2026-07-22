package com.biblelib.core.designsystem.customization

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.biblelib.core.designsystem.R

data class AppFontOption(
    val id: String,
    val displayName: String,
    val family: FontFamily,
)

object AppFontFamilies {
    val DEFAULT = AppFontOption("default", "Roboto (System)", FontFamily.Default)
    private val MONOSPACE = AppFontOption("mono_space", "Mono Space (System)", FontFamily.Monospace)
    private val SANS = AppFontOption("sans_serif", "Sans Serif (System)", FontFamily.SansSerif)

    private val OPEN_SANS =
        AppFontOption("open_sans", "Open Sans", FontFamily(Font(R.font.open_sans)))
    private val LATO = AppFontOption("lato", "Lato", FontFamily(Font(R.font.lato)))
    private val NUNITO = AppFontOption("nunito", "Nunito", FontFamily(Font(R.font.nunito)))
    private val POPPINS = AppFontOption("poppins", "Poppins", FontFamily(Font(R.font.poppins)))
    private val OSWALD = AppFontOption("oswald", "Oswald", FontFamily(Font(R.font.oswald)))
    private val TREBUCHET =
        AppFontOption("trebuchet", "Trebuchet MS", FontFamily(Font(R.font.trebuchet)))
    private val ROSEMARY = AppFontOption("rosemary", "Rosemary", FontFamily(Font(R.font.rosemary)))
    private val COOLJAZZ = AppFontOption("cooljazz", "Cool jazz", FontFamily(Font(R.font.cooljazz)))
    private val UBUNTUR = AppFontOption("ubuntu_r", "Ubuntu Regular", FontFamily(Font(R.font.ubuntu_r)))
    private val ROBOTOR = AppFontOption("roboto_r", "Roboto Regular", FontFamily(Font(R.font.roboto_r)))

    val ALL: List<AppFontOption> = listOf(
        DEFAULT,
        MONOSPACE,
        OPEN_SANS,
        LATO,
        NUNITO,
        POPPINS,
        OSWALD,
        SANS,
        TREBUCHET,
        ROSEMARY,
        UBUNTUR,
        ROBOTOR,
        COOLJAZZ
    )

    fun byId(id: String): AppFontOption = ALL.find { it.id == id } ?: DEFAULT
}

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
    private val SERIF = AppFontOption("serif", "Serif (System)", FontFamily.Serif)
    private val SANS = AppFontOption("sans_serif", "Sans Serif (System)", FontFamily.SansSerif)

    private val OPEN_SANS = AppFontOption("open_sans", "Open Sans", FontFamily(Font(R.font.open_sans)))
    private val LATO = AppFontOption("lato", "Lato", FontFamily(Font(R.font.lato)))
    private val MONTSERRAT = AppFontOption("montserrat", "Montserrat", FontFamily(Font(R.font.montserrat)))
    private val MERRIWEATHER = AppFontOption("merriweather", "Merriweather", FontFamily(Font(R.font.merriweather)))
    private val PLAYFAIR = AppFontOption("playfair_display", "Playfair Display", FontFamily(Font(R.font.playfair_display)))
    private val LORA = AppFontOption("lora", "Lora", FontFamily(Font(R.font.lora)))
    private val PT_SERIF = AppFontOption("pt_serif", "PT Serif", FontFamily(Font(R.font.pt_serif)))
    private val NUNITO = AppFontOption("nunito", "Nunito", FontFamily(Font(R.font.nunito)))
    private val POPPINS = AppFontOption("poppins", "Poppins", FontFamily(Font(R.font.poppins)))
    private val OSWALD = AppFontOption("oswald", "Oswald", FontFamily(Font(R.font.oswald)))
    private val NOTO_SERIF = AppFontOption("noto_serif", "Noto Serif", FontFamily(Font(R.font.noto_serif)))

    val ALL: List<AppFontOption> = listOf(
        DEFAULT, OPEN_SANS, LATO, MONTSERRAT, MERRIWEATHER, PLAYFAIR, LORA,
        PT_SERIF, NUNITO, POPPINS, OSWALD, NOTO_SERIF, SERIF, SANS,
    )

    fun byId(id: String): AppFontOption = ALL.find { it.id == id } ?: DEFAULT
}

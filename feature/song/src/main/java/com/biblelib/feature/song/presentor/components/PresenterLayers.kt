package com.biblelib.feature.song.presentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biblelib.core.common.utils.AppFonts
import com.biblelib.core.ui.sample.SampleIndicators
import com.biblelib.core.ui.sample.SampleVerses
import com.biblelib.feature.song.presentor.PresenterViewModel

@Composable
fun PresenterLayers(
    verses: List<String>,
    indicators: List<String>,
    horizontalSlides: Boolean = false,
    fontSize: Float = AppFonts.DEFAULT_FONT_SP,
    cornerOverlay: (@Composable () -> Unit)? = null,
) {
    val pagerState = rememberPagerState { verses.size }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PagerView(
            pagerState = pagerState,
            verses = verses,
            modifier = Modifier.weight(1f),
            horizontalSlides = horizontalSlides,
            fontSize = fontSize,
            cornerOverlay = cornerOverlay,
        )

        VerseIndicators(
            pagerState = pagerState,
            indicators = indicators,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPresenterContent() {
    PresenterLayers(
        verses = SampleVerses,
        indicators = SampleIndicators,
    )
}
package com.biblelib.feature.settings.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.ui.components.action.AppTopBar
import com.biblelib.feature.settings.view.components.SettingsGroupLabel
import com.biblelib.feature.settings.view.components.SettingsNavRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                showGoBack = true,
                onNavIconClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SettingsGroupLabel("Preferences") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Theme, reader background",
                    onClick = { navController.navigate(Routes.APPEARANCE_SETTINGS) },
                )
            }
            item {
                SettingsNavRow(
                    icon = Icons.Default.MenuBook,
                    title = "Reading",
                    subtitle = "Font, size",
                    onClick = { navController.navigate(Routes.READING_SETTINGS) },
                )
            }
            item { SettingsGroupLabel("RESET") }
            item {
                SettingsNavRow(
                    icon = Icons.Default.DataUsage,
                    title = "App Data",
                    subtitle = "Bookmarks, notes, history & storage",
                    onClick = { navController.navigate(Routes.DATA_SETTINGS) },
                )
            }
        }
    }
}

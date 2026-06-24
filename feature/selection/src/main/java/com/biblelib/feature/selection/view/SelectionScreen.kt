package com.biblelib.feature.selection.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.biblelib.core.common.entity.UiState
import com.biblelib.core.common.utils.Routes
import com.biblelib.core.data.repos.ThemeRepo
import com.biblelib.core.ui.components.indicators.BibleCardShimmer
import com.biblelib.core.ui.components.indicators.ErrorState
import com.biblelib.feature.selection.SelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    themeRepo: ThemeRepo,
) {
    val uiState by viewModel.uiState.collectAsState()
    val bibles  by viewModel.bibles.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchBibles() }

    // Navigate to reader when saving is done
    LaunchedEffect(uiState) {
        if (uiState is UiState.Saved) {
            navController.navigate(Routes.reader()) {
                popUpTo(Routes.SELECTION) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Choose Your Bibles", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Select 1–3 versions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        bottomBar = {
            ProceedBar(
                selectedCount = viewModel.selectedCount,
                maxSelections = viewModel.maxSelections,
                canProceed    = viewModel.canProceed(),
                isSaving      = uiState is UiState.Saving,
                onProceed     = viewModel::saveSelectionAndDownload,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState) {
                is UiState.Loading -> BibleCardShimmer()

                is UiState.Error -> ErrorState(
                    message = (uiState as UiState.Error).message,
                    onRetry = viewModel::fetchBibles
                )

                is UiState.Saving -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Downloading primary Bible…", style = MaterialTheme.typography.bodyMedium)
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(bibles, key = { _, b -> b.data.abbreviation }) { index, item ->
                        val isSelected = item.isSelected
                        val isDisabled = !isSelected && viewModel.selectedCount >= viewModel.maxSelections

                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 40L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                        ) {
                            BibleListItem(
                                name        = item.data.name,
                                description = item.data.description,
                                abbreviation= item.data.abbreviation,
                                language    = item.data.language.name,
                                isSelected  = isSelected,
                                isDisabled  = isDisabled,
                                onClick     = { viewModel.toggleSelection(item.data.abbreviation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BibleListItem(
    name: String,
    description: String,
    abbreviation: String,
    language: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .scale(scale)
            .clickable(enabled = !isDisabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected  -> MaterialTheme.colorScheme.primaryContainer
                isDisabled  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else        -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Abbreviation badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = abbreviation.uppercase().take(3),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description.ifEmpty { language },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            // Checkmark
            AnimatedVisibility(visible = isSelected, enter = scaleIn(), exit = scaleOut()) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ProceedBar(
    selectedCount: Int,
    maxSelections: Int,
    canProceed: Boolean,
    isSaving: Boolean,
    onProceed: () -> Unit,
) {
    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Slot indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(maxSelections) { i ->
                    val filled = i < selectedCount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
            Text(
                text = "$selectedCount / $maxSelections selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Button(
                onClick = onProceed,
                enabled = canProceed && !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isSaving) "Downloading…" else "Continue")
            }
        }
    }
}

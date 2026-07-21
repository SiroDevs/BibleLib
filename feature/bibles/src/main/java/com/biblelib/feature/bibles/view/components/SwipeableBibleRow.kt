package com.biblelib.feature.bibles.view.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biblelib.core.database.model.BibleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableBibleRow(
    bible: BibleEntity,
    progress: Float?,
    isSecondary: Boolean,
    onDelete: () -> Unit,
    onToggleSecondary: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onContinueDownload: (() -> Unit)? = null,
    onRestartDownload: (() -> Unit)? = null,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> onToggleSecondary?.invoke()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        },
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = onToggleSecondary != null,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete",
                    alignment = Alignment.CenterEnd,
                )

                SwipeToDismissBoxValue.StartToEnd -> SwipeActionBackground(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = if (isSecondary) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                    contentDescription = if (isSecondary) "Remove from secondary" else "Add to secondary",
                    alignment = Alignment.CenterStart,
                )

                SwipeToDismissBoxValue.Settled -> {}
            }
        },
    ) {
        val downloadProgress = progress ?: bible.downloadProgress

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (!bible.isDownloaded && !bible.downloadFailed) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant
                                ),
                                startX = (shimmerProgress.value - 0.5f).coerceIn(0f, 1f),
                                endX = (shimmerProgress.value).coerceIn(0f, 1f)
                            )
                        )
                    } else Modifier
                )
        ) {
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bible.abbreviation.uppercase().take(3),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        leadingContent?.invoke()
                    }
                },
                headlineContent = { Text(bible.name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text("${bible.abbreviation.uppercase()} BIBLE") },
                trailingContent = {
                    if (bible.downloadFailed) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 5.dp)
                        ) {
                            TextButton(
                                onClick = { onRestartDownload?.invoke() },
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                Text("Restart")
                            }
                            TextButton(
                                onClick = { onContinueDownload?.invoke() },
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                Text("Continue")
                            }
                        }
                    }
                }
            )

            if (bible.downloadFailed) {
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        "DOWNLOAD FAILED · ${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else if (!bible.isDownloaded) {
                Text(
                    "Downloading ... ${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SwipeActionBackground(
    color: Color,
    contentColor: Color,
    icon: ImageVector,
    contentDescription: String,
    alignment: Alignment,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(icon, contentDescription, tint = contentColor)
    }
}

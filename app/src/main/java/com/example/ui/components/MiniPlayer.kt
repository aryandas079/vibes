package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Song

@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: (() -> Unit)? = null,
    onSeek: ((Long) -> Unit)? = null,
    onExpand: () -> Unit,
    onOpenQueue: (() -> Unit)? = null,
    upcomingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectiveDuration = if (durationMs > 0) durationMs else 30000L
    val view = LocalView.current

    AnimatedVisibility(
        visible = song != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (song == null) return@AnimatedVisibility

        val progress = if (effectiveDuration > 0) {
            (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp))
                .clickable(onClick = onExpand)
                .testTag("mini_player_container")
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album artwork thumbnail
                    AsyncImage(
                        model = song.artworkUrl,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title, Artist and 30s Time Indicator
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            color = colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = song.artist,
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = "• ${formatMiniTime(currentPositionMs)}/${formatMiniTime(effectiveDuration)}",
                                color = colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Previous button (if provided)
                    if (onPrevious != null) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onPrevious()
                            },
                            modifier = Modifier.size(36.dp).testTag("mini_player_prev")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Play/Pause button
                    if (isBuffering) {
                        CircularProgressIndicator(
                            color = colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(8.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onTogglePlayPause()
                            },
                            modifier = Modifier.size(40.dp).testTag("mini_player_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Next button
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            onNext()
                        },
                        modifier = Modifier.size(36.dp).testTag("mini_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Queue Drawer button accessible directly from the playback bar
                    if (onOpenQueue != null) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onOpenQueue()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("playback_bar_queue_button")
                        ) {
                            if (upcomingCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = colorScheme.primary,
                                            contentColor = colorScheme.onPrimary
                                        ) {
                                            Text(
                                                text = if (upcomingCount > 9) "9+" else "$upcomingCount",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = "Playback Queue",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                    contentDescription = "Playback Queue",
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Interactive Progress Bar along the bottom of the Mini Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .pointerInput(effectiveDuration) {
                            detectTapGestures { offset ->
                                val width = size.width
                                if (width > 0 && onSeek != null) {
                                    val ratio = (offset.x / width).coerceIn(0f, 1f)
                                    onSeek((ratio * effectiveDuration).toLong())
                                }
                            }
                        }
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = colorScheme.primary,
                        trackColor = colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun formatMiniTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

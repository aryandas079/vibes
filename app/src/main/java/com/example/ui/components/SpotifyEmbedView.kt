package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Song

// Authentic Spotify Embed Palette
private val SpotifyGreenBrand = Color(0xFF1DB954)
private val SpotifyDarkCanvas = Color(0xFF121212)
private val SpotifyCardBg = Color(0xFF181818)
private val SpotifyBorder = Color(0xFF282828)
private val SpotifyMutedText = Color(0xFFB3B3B3)
private val SpotifySubtleText = Color(0xFF727272)

/**
 * Spotify Embed Dialog - Authentic In-App Interactive Player.
 *
 * Renders an authentic Spotify Embed widget UI that plays the actual song audio preview
 * completely inside the app. Provides interactive Play/Pause, animated Spotify equalizer
 * bars, scrubbable progress bar, and seek controls without external redirects.
 */
@Composable
fun SpotifyEmbedDialog(
    song: Song,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    currentPositionMs: Long = 0L,
    durationMs: Long = 30000L,
    onTogglePlayPause: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSeekBy: (Long) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val effectiveDuration = if (durationMs > 0L) durationMs else 30000L

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionRatio by remember { mutableFloatStateOf(0f) }

    val displayRatio = if (isScrubbing) {
        scrubPositionRatio
    } else {
        (currentPositionMs.toFloat() / effectiveDuration).coerceIn(0f, 1f)
    }

    val displayPositionMs = if (isScrubbing) {
        (scrubPositionRatio * effectiveDuration).toLong()
    } else {
        currentPositionMs.coerceIn(0L, effectiveDuration)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = SpotifyDarkCanvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF333333))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header Row: Spotify branding & close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_spotify),
                        contentDescription = "Spotify Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SPOTIFY EMBED",
                                color = SpotifyGreenBrand,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "•",
                                color = SpotifySubtleText,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "IN-APP PREVIEW",
                                color = SpotifyMutedText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Interactive Song Player",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SpotifyMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // AUTHENTIC SPOTIFY EMBED CARD
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF222222),
                                SpotifyCardBg
                            )
                        )
                    )
                    .border(1.dp, SpotifyBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Bar inside Embed: Badge and Live Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo_spotify),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Spotify",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Live audio status tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isPlaying) SpotifyGreenBrand.copy(alpha = 0.18f)
                                    else Color(0xFF2A2A2A)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when {
                                    isBuffering -> "Buffering..."
                                    isPlaying -> "▶ Playing Preview"
                                    else -> "⏸ Paused"
                                },
                                color = if (isPlaying) SpotifyGreenBrand else SpotifyMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Track Info Row: Album Art + Titles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Artwork with subtle shadow/border
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                        ) {
                            MusicaImage(
                                model = song.artworkUrl,
                                contentDescription = song.title,
                                titlePlaceholder = song.title,
                                modifier = Modifier.size(82.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = song.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = song.artist,
                                color = SpotifyMutedText,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (song.album.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (song.releaseYear.isNotBlank()) "${song.album} • ${song.releaseYear}" else song.album,
                                    color = SpotifySubtleText,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Spotify Equalizer Bars
                    SpotifyEqualizerBars(
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .padding(horizontal = 4.dp),
                        barCount = 20,
                        color = SpotifyGreenBrand
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive Progress Scrubber Slider
                    Slider(
                        value = displayRatio,
                        onValueChange = { frac ->
                            isScrubbing = true
                            scrubPositionRatio = frac
                        },
                        onValueChangeFinished = {
                            isScrubbing = false
                            onSeek((scrubPositionRatio * effectiveDuration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyGreenBrand,
                            activeTrackColor = SpotifyGreenBrand,
                            inactiveTrackColor = Color(0xFF383838)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Timestamps Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(displayPositionMs),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isScrubbing) SpotifyGreenBrand else SpotifyMutedText
                        )

                        Text(
                            text = "30s Audio Stream",
                            fontSize = 10.5.sp,
                            color = SpotifySubtleText
                        )

                        Text(
                            text = formatTime(effectiveDuration),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpotifyMutedText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controls Row: -5s, Restart, Play/Pause, +5s
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind -5s
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .border(1.dp, Color(0xFF333333), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSeekBy(-5000L)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "-5s",
                                color = SpotifyMutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Restart 0:00
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .border(1.dp, Color(0xFF333333), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSeek(0L)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Restart Preview",
                                tint = SpotifyMutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Central Spotify Green Play/Pause Button
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreenBrand)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTogglePlayPause()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(26.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        // Forward +5s
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .border(1.dp, Color(0xFF333333), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSeekBy(5000L)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+5s",
                                color = SpotifyMutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // In-App Guarantee Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF16171D))
                    .border(1.dp, Color(0xFF22242D), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✓",
                    color = SpotifyGreenBrand,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Streaming actual audio preview in-app only • No external apps needed",
                    color = SpotifyMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Action: Continue in Vibes
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreenBrand,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_spotify),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue Listening in Vibes",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary Action: Close
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Done",
                    color = SpotifyMutedText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Animated Spotify Equalizer Spectrum Bars.
 * Oscillates smoothly when playing and rests quietly when paused.
 */
@Composable
fun SpotifyEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 20,
    color: Color = SpotifyGreenBrand
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpotifyEqualizer")

    val animFractions = List(barCount) { index ->
        val duration = remember(index) { 320 + (index % 6) * 110 }
        val delay = remember(index) { (index * 60) % 280 }
        if (isPlaying) {
            val anim by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, delayMillis = delay, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            anim
        } else {
            0.15f
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animFractions.forEachIndexed { idx, frac ->
            val heightMultiplier = when (idx % 5) {
                0 -> 1.0f
                1 -> 0.7f
                2 -> 0.95f
                3 -> 0.6f
                else -> 0.85f
            }
            val effectiveFrac = (frac * heightMultiplier).coerceIn(0.12f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((26.dp * effectiveFrac).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(color.copy(alpha = if (isPlaying) 0.95f else 0.35f))
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Optional intent launcher preserved for API compatibility.
 */
fun openSpotifyIntent(context: Context, song: Song) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("spotify:track:${song.validSpotifyTrackId}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(song.spotifyUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(webIntent)
    }
}

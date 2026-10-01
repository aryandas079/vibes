package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.liquidGlassEffect
import com.example.util.VibesHaptics

/**
 * Elegant Top Status Bar Indicator for Currently Playing Song.
 * Inspired by Spotify & Apple Music Dynamic Island / top bar indicators.
 * Floats seamlessly below the system status bar, showing track art, animated
 * equalizer spectrum, live track details, and quick play/pause toggle.
 */
@Composable
fun StatusBarSongIndicator(
    song: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean = false,
    isVisible: Boolean,
    onOpenNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    AnimatedVisibility(
        visible = isVisible && song != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        if (song == null) return@AnimatedVisibility

        Box(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .liquidGlassEffect(shape = RoundedCornerShape(22.dp), elevation = 6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1B1D28),
                                Color(0xFF141620)
                            )
                        )
                    )
                    .border(1.2.dp, StormSlateBorder, RoundedCornerShape(22.dp))
                    .clickable {
                        VibesHaptics.strongClick(context, view)
                        onOpenNowPlaying()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Miniature Album Art with tiny rounded border
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF383C4E), CircleShape)
                    ) {
                        MusicaImage(
                            model = song.artworkUrl,
                            contentDescription = song.title,
                            titlePlaceholder = song.title,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 3-Bar Mini Animated Equalizer
                    MiniEqualizerBars(
                        isPlaying = isPlaying,
                        modifier = Modifier.width(14.dp).height(14.dp)
                    )

                    // Song Title & Artist text
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = song.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteSmoke,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = WhiteSmokeMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = song.artist,
                            fontSize = 11.sp,
                            color = WhiteSmokeMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Quick Mini Play/Pause button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222534))
                            .border(1.dp, Color(0xFF35394E), CircleShape)
                            .clickable {
                                VibesHaptics.playPause(context, view)
                                onTogglePlayPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                color = SpotifyGreen,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) Color(0xFF1DB954) else WhiteSmoke,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_eq")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(240, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    val barHeights = if (isPlaying) listOf(h1, h2, h3) else listOf(0.2f, 0.25f, 0.2f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        barHeights.forEach { frac ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((14.dp * frac).coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isPlaying) Color(0xFF1DB954) else WhiteSmokeMuted.copy(alpha = 0.5f))
            )
        }
    }
}

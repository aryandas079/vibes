package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SyncedLyricLine
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeDim
import com.example.ui.theme.WhiteSmokeLight
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect

/**
 * Custom clip shape that clips rectangular content horizontally from 0 to [progress].
 * This creates the iconic karaoke text fill sweep in real-time as the song plays.
 */
class KaraokeProgressClipShape(private val progress: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val sweepWidth = (size.width * progress.coerceIn(0f, 1f))
        return Outline.Rectangle(Rect(0f, 0f, sweepWidth, size.height))
    }
}

/**
 * Real-Time Karaoke-Style Lyric Line with text progressive sweep highlight,
 * synchronized micro-progress bar, live equalizer audio wave, and timing indicator.
 */
@Composable
fun KaraokeLyricLineView(
    line: SyncedLyricLine,
    lineIndex: Int,
    activeIndex: Int,
    currentPositionMs: Long,
    nextTimeMs: Long,
    isPlaying: Boolean,
    selectedLanguage: String,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    val isActive = lineIndex == activeIndex
    val isPast = lineIndex < activeIndex
    val isFuture = lineIndex > activeIndex

    // Calculate progress within this specific line
    val lineDurationMs = (nextTimeMs - line.timeMs).coerceAtLeast(600L)
    val lineProgress: Float = when {
        isPast -> 1.0f
        isFuture -> 0.0f
        else -> {
            ((currentPositionMs - line.timeMs).toFloat() / lineDurationMs.toFloat()).coerceIn(0f, 1f)
        }
    }

    // Scale animation for active line
    val lineScale by animateFloatAsState(
        targetValue = if (isActive) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
        label = "karaoke_line_scale"
    )

    // Animated glow pulse for active karaoke state
    val infiniteTransition = rememberInfiniteTransition(label = "karaoke_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val containerModifier = if (isActive) {
        Modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        SpotifyGreen.copy(alpha = glowAlpha),
                        Color(0xFF06B6D4).copy(alpha = glowAlpha),
                        Color(0xFFC084FC).copy(alpha = glowAlpha * 0.7f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(lineScale)
            .then(containerModifier)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onSeek(line.timeMs)
            }
            .testTag("karaoke_line_$lineIndex"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Active Line Karaoke Badge & Audio Waves
            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // Left: Live Equalizer Visualizer Bars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        KaraokeWaveBars(isPlaying = isPlaying)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE KARAOKE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = SpotifyGreen,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Right: Real-Time Millisecond Timing (e.g. 1.4s / 3.2s)
                    val elapsedSec = ((currentPositionMs - line.timeMs).coerceAtLeast(0L) / 1000f)
                    val totalSec = (lineDurationMs / 1000f)
                    Text(
                        text = String.format("%.1fs / %.1fs", elapsedSec, totalSec),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC084FC)
                    )
                }
            }

            // Real-time Karaoke Progressive Text Highlighting
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                // Layer 1: Dimmed Base Text (Unhighlighted)
                val baseTextColor = when {
                    isActive -> Color.White.copy(alpha = 0.30f)
                    Math.abs(lineIndex - activeIndex) == 1 -> Color.White.copy(alpha = 0.60f)
                    isPast -> Color.White.copy(alpha = 0.50f)
                    else -> Color.White.copy(alpha = 0.25f)
                }

                Text(
                    text = line.text,
                    fontSize = if (isActive) 23.sp else 18.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                    color = baseTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isActive) 30.sp else 25.sp,
                    letterSpacing = if (isActive) (-0.2).sp else 0.sp
                )

                // Layer 2: Glowing Active Highlighted Text (Clipped horizontally by lineProgress)
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(KaraokeProgressClipShape(lineProgress))
                    ) {
                        Text(
                            text = line.text,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF0FDF4), // Crisp glowing white with faint mint tint
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp,
                            letterSpacing = (-0.2).sp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (isPast) {
                    // Line has already been sung in full
                    Text(
                        text = line.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WhiteSmokeSoft,
                        textAlign = TextAlign.Center,
                        lineHeight = 25.sp
                    )
                }
            }

            // Real-Time Karaoke Progress Bar Track (directly under the active line)
            if (isActive) {
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    // Glowing Progressive Fill Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(lineProgress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        SpotifyGreen,
                                        Color(0xFF22D3EE),
                                        Color(0xFFC084FC)
                                    )
                                )
                            )
                    )

                    // Leading cursor bead riding the active edge
                    if (lineProgress > 0.02f && lineProgress < 0.99f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(lineProgress)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .shadow(4.dp, CircleShape)
                            )
                        }
                    }
                }
            }

            // Romanized Reading (if available)
            if (!line.romanized.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line.romanized!!,
                    fontSize = 12.5.sp,
                    color = if (isActive) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                )
            }

            // Live Translation (if selected language is not Original)
            if (!line.translation.isNullOrBlank() && selectedLanguage != "Original") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = line.translation!!,
                    fontSize = if (isActive) 14.5.sp else 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) Color(0xFF6EE7B7) else colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

/**
 * 3-bar lively equalizer wave animation for karaoke singing indication
 */
@Composable
fun KaraokeWaveBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_bars")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.height(16.dp)
    ) {
        val h1 = if (isPlaying) bar1Height.dp else 4.dp
        val h2 = if (isPlaying) bar2Height.dp else 8.dp
        val h3 = if (isPlaying) bar3Height.dp else 5.dp

        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1)
                .clip(RoundedCornerShape(2.dp))
                .background(SpotifyGreen)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF22D3EE))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFC084FC))
        )
    }
}

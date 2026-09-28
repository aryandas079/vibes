package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LyricsData
import com.example.model.Song
import com.example.model.SyncedLyricLine
import com.example.ui.components.KaraokeLyricLineView
import com.example.ui.components.KaraokeWaveBars
import com.example.ui.components.PlayerTab
import com.example.ui.components.PlayerTabNavigation
import com.example.ui.components.StreamingHubsSection
import com.example.ui.theme.*
import com.example.util.LyricsEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean = false,
    isLooping: Boolean = false,
    isFavorite: Boolean = false,
    selectedTab: PlayerTab = PlayerTab.NOW_PLAYING,
    onTabSelected: (PlayerTab) -> Unit = {},
    lyricsData: LyricsData? = null,
    isLyricsLoading: Boolean = false,
    selectedLanguage: String = "Original",
    onSelectLanguage: (String) -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekBy: ((Long) -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onToggleShuffle: (() -> Unit)? = null,
    onToggleLoop: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onClose: () -> Unit,
    onOpenQueue: (() -> Unit)? = null,
    onOpenSpotifyEmbed: () -> Unit,
    onOpenLyrics: () -> Unit = { onTabSelected(PlayerTab.LYRICS) },
    onOpenEqualizer: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onArtistClick: ((String) -> Unit)? = null
) {
    if (song == null) return

    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: Modern slide-down button, Slim tab switcher, and Sleek actions (Equalizer & Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onClose()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .liquidGlassEffect(shape = CircleShape, elevation = 1.dp)
                        .testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                PlayerTabNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onTabSelected(tab)
                    },
                    isLyricsSynced = lyricsData?.syncedLines?.isNotEmpty() ?: true,
                    modifier = Modifier
                        .width(180.dp)
                        .testTag("player_tab_bar")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onOpenEqualizer != null) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onOpenEqualizer()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 1.dp)
                                .testTag("now_playing_equalizer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Audio Equalizer",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onOpenQueue != null) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onOpenQueue()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 1.dp)
                                .testTag("now_playing_queue_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Listening Session Queue",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Tab-Based View Switching (Now Playing vs Real-Time Synced Lyrics)
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState == PlayerTab.LYRICS) {
                        (slideInVertically { height -> height / 3 } + fadeIn(tween(250))).togetherWith(
                            slideOutVertically { height -> -height / 3 } + fadeOut(tween(250))
                        )
                    } else {
                        (slideInVertically { height -> -height / 3 } + fadeIn(tween(250))).togetherWith(
                            slideOutVertically { height -> height / 3 } + fadeOut(tween(250))
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(selectedTab) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -35f && selectedTab == PlayerTab.NOW_PLAYING) {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onTabSelected(PlayerTab.LYRICS)
                            } else if (dragAmount > 35f && selectedTab == PlayerTab.LYRICS) {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onTabSelected(PlayerTab.NOW_PLAYING)
                            }
                        }
                    },
                label = "player_tab_content"
            ) { tab ->
                when (tab) {
                    PlayerTab.NOW_PLAYING -> {
                        NowPlayingViewContent(
                            song = song,
                            isPlaying = isPlaying,
                            isBuffering = isBuffering,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            isShuffle = isShuffle,
                            isLooping = isLooping,
                            isFavorite = isFavorite,
                            lyricsData = lyricsData,
                            onTogglePlayPause = onTogglePlayPause,
                            onSeek = onSeek,
                            onSeekBy = onSeekBy,
                            onNext = onNext,
                            onPrevious = onPrevious,
                            onToggleShuffle = onToggleShuffle,
                            onToggleLoop = onToggleLoop,
                            onToggleFavorite = onToggleFavorite,
                            onOpenLyrics = { onTabSelected(PlayerTab.LYRICS) },
                            onOpenSpotifyEmbed = onOpenSpotifyEmbed,
                            onArtistClick = onArtistClick
                        )
                    }

                    PlayerTab.LYRICS -> {
                        LyricsViewContent(
                            song = song,
                            lyricsData = lyricsData,
                            isLyricsLoading = isLyricsLoading,
                            selectedLanguage = selectedLanguage,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            isPlaying = isPlaying,
                            onSeek = onSeek,
                            onTogglePlayPause = onTogglePlayPause,
                            onNext = { onNext?.invoke() },
                            onPrevious = { onPrevious?.invoke() },
                            onSelectLanguage = onSelectLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingViewContent(
    song: Song,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    isLooping: Boolean,
    isFavorite: Boolean = false,
    lyricsData: LyricsData? = null,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekBy: ((Long) -> Unit)?,
    onNext: (() -> Unit)?,
    onPrevious: (() -> Unit)?,
    onToggleShuffle: (() -> Unit)?,
    onToggleLoop: (() -> Unit)?,
    onToggleFavorite: (() -> Unit)? = null,
    onOpenLyrics: () -> Unit,
    onOpenSpotifyEmbed: () -> Unit,
    onArtistClick: ((String) -> Unit)?
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectiveDuration = if (durationMs > 0) durationMs else 30000L
    val view = LocalView.current

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionRatio by remember { mutableFloatStateOf(0f) }

    val currentRatio = if (effectiveDuration > 0) {
        (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayRatio = if (isScrubbing) scrubPositionRatio else currentRatio
    val displayPositionMs = if (isScrubbing) (scrubPositionRatio * effectiveDuration).toLong() else currentPositionMs

    val syncedLines = lyricsData?.syncedLines ?: emptyList()
    val activeIndex = remember(currentPositionMs, syncedLines) {
        if (syncedLines.isEmpty()) -1
        else {
            val idx = syncedLines.indexOfLast { currentPositionMs >= it.timeMs }
            if (idx == -1) 0 else idx
        }
    }
    val activeLineText: String = if (activeIndex in syncedLines.indices) {
        syncedLines[activeIndex].text
    } else {
        lyricsData?.plainLyrics?.lines()?.firstOrNull { it.isNotBlank() } ?: "Tap or drag up for real-time lyrics"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
    ) {
        // Compact Album Art with Subtle Glass Glow
        item {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .liquidGlassEffect(shape = RoundedCornerShape(22.dp), elevation = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                )

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StormBlackBg.copy(alpha = 0.70f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "PLAYING",
                                color = SpotifyGreen,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Song Title & Artist with Favorite Heart Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${song.artist} • ${song.album}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(enabled = onArtistClick != null) {
                            onArtistClick?.invoke(song.artist)
                        }
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite?.invoke() },
                    modifier = Modifier
                        .size(42.dp)
                        .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                        .testTag("player_heart_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Saved in Favorites" else "Save to Favorites",
                        tint = if (isFavorite) WhiteSmoke else colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Streamlined Audio Player Card (Seek Bar & Playback Controls)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("audio_player_seek_bar")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(displayPositionMs),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isScrubbing) colorScheme.primary else colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = formatTime(effectiveDuration),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleShuffle?.invoke()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onPrevious?.invoke()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Song",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            colorScheme.primary,
                                            colorScheme.primary.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .border(1.5.dp, WhiteSmoke.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTogglePlayPause()
                                }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    color = colorScheme.onPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(26.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onNext?.invoke()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Song",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleLoop?.invoke()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Repeat",
                                tint = if (isLooping) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Draggable Interactive Synced Lyrics Card Drawer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
                    .border(1.dp, SpotifyGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onOpenLyrics()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("show_lyrics_tab_button")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag handle pill bar
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        KaraokeWaveBars(isPlaying = isPlaying)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeLineText,
                            modifier = Modifier.weight(1f, fill = false),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Drag or tap for real-time lyrics",
                            tint = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Drag or tap for real-time lyrics",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.60f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Compact Metadata Row
        item {
            val producers = when {
                song.title.contains("Cruel Summer", ignoreCase = true) -> "Jack Antonoff, St. Vincent"
                song.title.contains("Shape", ignoreCase = true) -> "Steve Mac, Ed Sheeran"
                song.title.contains("Blinding", ignoreCase = true) -> "Max Martin, Oscar Holter"
                song.title.contains("Birds of a Feather", ignoreCase = true) -> "FINNEAS"
                song.title.contains("Espresso", ignoreCase = true) -> "Julian Bunetta"
                song.title.contains("Monaco", ignoreCase = true) -> "Tainy, MAG"
                song.title.contains("As It Was", ignoreCase = true) -> "Kid Harpoon, Tyler Johnson"
                song.title.contains("Die With A Smile", ignoreCase = true) -> "Bruno Mars, Andrew Watt"
                song.title.contains("Tabun", ignoreCase = true) || song.artist.contains("YOASOBI", ignoreCase = true) -> "Ayase"
                song.title.contains("Starboy", ignoreCase = true) -> "Daft Punk, Doc McKinney"
                song.artist.contains("Drake", ignoreCase = true) -> "Noah '40' Shebib"
                song.artist.contains("Taylor", ignoreCase = true) -> "Jack Antonoff, Aaron Dessner"
                song.artist.contains("Billie", ignoreCase = true) -> "FINNEAS"
                else -> "${song.artist.split(" ").firstOrNull() ?: "Producer"}, Hitmaker"
            }

            val views = when {
                song.title.contains("Cruel Summer", ignoreCase = true) -> "2.4B"
                song.title.contains("Shape", ignoreCase = true) -> "3.6B"
                song.title.contains("Blinding", ignoreCase = true) -> "4.5B"
                song.title.contains("Birds of a Feather", ignoreCase = true) -> "1.8B"
                song.title.contains("Espresso", ignoreCase = true) -> "1.7B"
                song.title.contains("Monaco", ignoreCase = true) -> "890M"
                song.title.contains("As It Was", ignoreCase = true) -> "3.1B"
                song.title.contains("Die With A Smile", ignoreCase = true) -> "1.3B"
                song.title.contains("One Dance", ignoreCase = true) -> "3.2B"
                song.title.contains("God's Plan", ignoreCase = true) -> "2.5B"
                song.title.contains("Tabun", ignoreCase = true) -> "420M"
                song.title.contains("Starboy", ignoreCase = true) -> "2.9B"
                else -> "850M"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Release", fontSize = 10.5.sp, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = song.releaseYear, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(colorScheme.onSurface.copy(alpha = 0.12f)))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f, fill = false)) {
                    Text(text = "Producers", fontSize = 10.5.sp, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = producers,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(colorScheme.onSurface.copy(alpha = 0.12f)))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Streams", fontSize = 10.5.sp, color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = views, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Major Music Streaming Platforms Deep Links Section
        item {
            StreamingHubsSection(
                song = song,
                onOpenSpotifyEmbed = onOpenSpotifyEmbed
            )
        }
    }
}

// =========================================================================
// Real-Time Auto-Scrolling Synchronized Karaoke Lyrics View
// =========================================================================
@Composable
private fun LyricsViewContent(
    song: Song,
    lyricsData: LyricsData?,
    isLyricsLoading: Boolean,
    selectedLanguage: String,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val languages = listOf("Original", "English", "Spanish", "Japanese", "Korean", "French", "German", "Hindi", "Chinese", "Italian")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Resolve preview-synced lyrics with exact millisecond precision
    val syncedLines: List<SyncedLyricLine> = remember(lyricsData, song, selectedLanguage) {
        val baseLines = run {
            val exact = LyricsEngine.getExactLyrics(song.title, song.artist)
            if (exact != null) {
                LyricsEngine.parseSyncedLyrics(exact)
            } else if (lyricsData != null && lyricsData.syncedLines.isNotEmpty()) {
                val filtered = lyricsData.syncedLines.filterNot {
                    it.text.contains("Elizabeth Taylor", ignoreCase = true) ||
                    it.text.contains("driving through the neon lights", ignoreCase = true)
                }
                LyricsEngine.alignSyncedLyricsForPreview(filtered, song.title, 30000L)
            } else if (lyricsData != null && lyricsData.plainLyrics.isNotBlank()) {
                LyricsEngine.plainToEstimatedSynced(lyricsData.plainLyrics, 30000L)
            } else {
                emptyList()
            }
        }

        if (selectedLanguage == "Original") {
            baseLines
        } else {
            baseLines.map { line ->
                val cached = lyricsData?.syncedLines?.firstOrNull { it.text == line.text }?.translation
                val trans = cached ?: LyricsEngine.translateLyricLine(line.text, selectedLanguage)
                line.copy(translation = trans)
            }
        }
    }

    val fullLyricsText: String = remember(song, lyricsData) {
        val fullExact = LyricsEngine.getFullLyrics(song.title, song.artist)
        if (!fullExact.isNullOrBlank()) {
            fullExact
        } else if (lyricsData != null && lyricsData.plainLyrics.isNotBlank()) {
            lyricsData.plainLyrics
        } else if (syncedLines.isNotEmpty()) {
            syncedLines.joinToString("\n") { it.text }
        } else {
            ""
        }
    }

    // Identify active lyric line in real-time based on preview playback position
    val activeIndex = remember(currentPositionMs, syncedLines) {
        if (syncedLines.isEmpty()) -1
        else {
            val idx = syncedLines.indexOfLast { currentPositionMs >= it.timeMs }
            if (idx == -1) 0 else idx
        }
    }

    val listState = rememberLazyListState()
    var isUserInteracting by remember { mutableStateOf(false) }
    var autoScrollEnabled by remember { mutableStateOf(true) }

    // Detect user manual scroll to temporarily pause auto-scroll without fighting user gesture
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isUserInteracting = true
        } else {
            delay(2500L) // After 2.5s of inactivity, auto-scroll smoothly re-engages
            isUserInteracting = false
        }
    }

    // Automatic smooth centering scroll synchronized to preview playback time
    LaunchedEffect(activeIndex, autoScrollEnabled, isUserInteracting) {
        if (autoScrollEnabled && !isUserInteracting && activeIndex in syncedLines.indices) {
            val targetIndex = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(
                index = targetIndex,
                scrollOffset = -120
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Track summary header inside lyrics view
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .liquidGlassEffect(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
            ) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = song.album,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Translation Language & Auto-Scroll Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Translation Language Dropdown Selector
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassEffect(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lang: $selectedLanguage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Language",
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang,
                                    fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == selectedLanguage) colorScheme.primary else colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSelectLanguage(lang)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Auto-Scroll Toggle Pill
            if (syncedLines.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (autoScrollEnabled) SpotifyGreen.copy(alpha = 0.18f)
                            else colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (autoScrollEnabled) SpotifyGreen.copy(alpha = 0.4f) else colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            autoScrollEnabled = !autoScrollEnabled
                            if (autoScrollEnabled && activeIndex in syncedLines.indices) {
                                scope.launch {
                                    listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0), -120)
                                }
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Auto Scroll",
                            tint = if (autoScrollEnabled) SpotifyGreen else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (autoScrollEnabled) "Auto-Scroll ON" else "Auto-Scroll OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (autoScrollEnabled) SpotifyGreen else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-Time Auto-Scrolling Karaoke Lyrics Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLyricsLoading && syncedLines.isEmpty() && fullLyricsText.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else if (syncedLines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lyrics not available for this track",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val effectiveDuration = if (durationMs > 0) durationMs else 30000L

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("synced_lyrics_list"),
                    contentPadding = PaddingValues(vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(syncedLines, key = { index, line -> "${line.timeMs}_$index" }) { index, line ->
                        val nextTimeMs = if (index < syncedLines.lastIndex) {
                            syncedLines[index + 1].timeMs
                        } else {
                            (line.timeMs + 4500L).coerceAtMost(effectiveDuration)
                        }

                        KaraokeLyricLineView(
                            line = line,
                            lineIndex = index,
                            activeIndex = activeIndex,
                            currentPositionMs = currentPositionMs,
                            nextTimeMs = nextTimeMs,
                            isPlaying = isPlaying,
                            selectedLanguage = selectedLanguage,
                            onSeek = { targetMs ->
                                onSeek(targetMs)
                                autoScrollEnabled = true
                                isUserInteracting = false
                            }
                        )
                    }
                }
            }

            // Floating snap pill if user scrolled away
            if (isUserInteracting && activeIndex in syncedLines.indices) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.primary)
                        .clickable {
                            isUserInteracting = false
                            autoScrollEnabled = true
                            scope.launch {
                                listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0), -120)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Snap to Current Line (${formatTime(currentPositionMs)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Bottom Compact Audio Player Scrubber for Lyrics View
        var isLyricsScrubbing by remember { mutableStateOf(false) }
        var lyricsScrubRatio by remember { mutableFloatStateOf(0f) }

        val effectiveDuration = if (durationMs > 0) durationMs else 30000L
        val currentProgRatio = if (effectiveDuration > 0) (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f) else 0f
        val displayLyricsRatio = if (isLyricsScrubbing) lyricsScrubRatio else currentProgRatio
        val displayLyricsPosMs = if (isLyricsScrubbing) (lyricsScrubRatio * effectiveDuration).toLong() else currentPositionMs

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column {
                Slider(
                    value = displayLyricsRatio,
                    onValueChange = { frac ->
                        isLyricsScrubbing = true
                        lyricsScrubRatio = frac
                    },
                    onValueChangeFinished = {
                        isLyricsScrubbing = false
                        onSeek((lyricsScrubRatio * effectiveDuration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(displayLyricsPosMs),
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable { onTogglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = StormBlackBg,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Text(
                        text = formatTime(effectiveDuration),
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickSeekChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.theme.*
import com.example.util.LyricsEngine

@Composable
fun LyricsScreen(
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
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val languages = listOf("Original", "English", "Spanish", "Japanese", "Korean", "French", "German", "Hindi", "Chinese", "Italian")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Resolve preview-synced lyrics with 100% precision
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

    // Full song lyrics text
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

    // Active line detection based on exact playback position
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

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isUserInteracting = true
        } else {
            kotlinx.coroutines.delay(2500L)
            isUserInteracting = false
        }
    }

    LaunchedEffect(activeIndex, autoScrollEnabled, isUserInteracting) {
        if (autoScrollEnabled && !isUserInteracting && activeIndex in syncedLines.indices) {
            val target = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(
                index = target,
                scrollOffset = -120
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar with Back Arrow and Track Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(42.dp)
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
                        fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(6.dp))

            // Controls Row: Translation Language Dropdown & Karaoke Sync Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Selector: "Translation Language"
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Translation",
                                fontSize = 10.5.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedLanguage,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Language",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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

                // Live Karaoke Progress Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SpotifyGreen.copy(alpha = 0.16f))
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KaraokeWaveBars(isPlaying = isPlaying)
                        Column {
                            Text(
                                text = "KARAOKE SYNC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = SpotifyGreen,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = if (activeIndex >= 0 && activeIndex < syncedLines.size)
                                    "Line ${activeIndex + 1}/${syncedLines.size}"
                                else "Ready",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lyrics Content Section
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
                    // Preview Synced Karaoke View
                    val effectiveDuration = if (durationMs > 0) durationMs else 30000L

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("karaoke_lyrics_list"),
                        contentPadding = PaddingValues(vertical = 36.dp),
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
            }

            // Bottom Player Bar with Liquid Glass
            var isLyricsScrubbing by remember { mutableStateOf(false) }
            var lyricsScrubRatio by remember { mutableFloatStateOf(0f) }

            val effectiveDuration = if (durationMs > 0) durationMs else 30000L
            val currentProgRatio = if (effectiveDuration > 0) (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f) else 0f
            val displayLyricsRatio = if (isLyricsScrubbing) lyricsScrubRatio else currentProgRatio
            val displayLyricsPosMs = if (isLyricsScrubbing) (lyricsScrubRatio * effectiveDuration).toLong() else currentPositionMs

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    // Progress Slider
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

                    // Timers & Controls Row
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onPrevious) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen)
                                    .clickable { onTogglePlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = StormBlackBg,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(onClick = onNext) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
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
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

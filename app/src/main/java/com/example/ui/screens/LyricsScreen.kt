package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LyricsData
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.liquidGlassEffect
import com.example.util.LyricsEngine

/**
 * Clean & Complete Song Lyrics Screen.
 * Displays clean plain-text song lyrics in stanzas matching standard song lyric layouts.
 * Features a dedicated "Translate" button that translates lyrics in reality, and a
 * "Get Song Meaning" button that uses Gemini AI to analyze the song's meaning and story.
 */
@Composable
fun LyricsScreen(
    song: Song,
    lyricsData: LyricsData?,
    isLyricsLoading: Boolean,
    selectedLanguage: String,
    selectedStanzaText: String?,
    selectedStanzaExplanation: String?,
    isExplainingStanza: Boolean,
    songMeaningExplanation: String? = null,
    isExplainingSongMeaning: Boolean = false,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onExplainStanza: (String) -> Unit,
    onClearStanzaExplanation: () -> Unit,
    onExplainSongMeaning: (String) -> Unit = {},
    onClearSongMeaning: () -> Unit = {},
    onSeek: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme
    val languages = listOf("Original", "English", "Spanish", "Japanese", "Korean", "French", "German", "Hindi", "Chinese", "Italian")
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showSongMeaningCard by remember { mutableStateOf(false) }

    // Complete full song lyrics text
    val fullLyricsText: String = remember(song, lyricsData, selectedLanguage) {
        val plain = if (lyricsData != null && lyricsData.songId == song.id) lyricsData.plainLyrics else ""
        val synced = if (lyricsData != null && lyricsData.songId == song.id) lyricsData.syncedLines.joinToString("\n") { it.text } else ""
        val apiLyrics = if (plain.isNotBlank()) plain else synced
        val fullExact = LyricsEngine.getFullLyrics(song.title, song.artist).orEmpty()

        when {
            selectedLanguage != "Original" && plain.isNotBlank() -> plain
            apiLyrics.isNotBlank() && apiLyrics.length >= fullExact.length -> apiLyrics
            fullExact.isNotBlank() -> fullExact
            apiLyrics.isNotBlank() -> apiLyrics
            else -> ""
        }
    }

    // Complete metadata (songwriters, publisher, date published, source attribution)
    val metadata: LyricsEngine.SongMetadata = remember(song, lyricsData) {
        val defaultMeta = LyricsEngine.getSongMetadata(song.title, song.artist)
        LyricsEngine.SongMetadata(
            songwriters = lyricsData?.songwriters?.ifBlank { null }
                ?: defaultMeta.songwriters.ifBlank { song.artist },
            publisher = lyricsData?.publisher?.ifBlank { null }
                ?: defaultMeta.publisher.ifBlank { song.album },
            publishDate = lyricsData?.publishDate?.ifBlank { null }
                ?: defaultMeta.publishDate.ifBlank { song.releaseYear },
            source = lyricsData?.source?.ifBlank { null }
                ?: defaultMeta.source.ifBlank { "Official Album Credits" }
        )
    }

    // Split lyrics into stanzas/sections
    val stanzas: List<String> = remember(fullLyricsText) {
        if (fullLyricsText.isBlank()) emptyList()
        else fullLyricsText.split(Regex("\n\n+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
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
            // Header Row with Back Button, Song Title & Subtitle ("Song by [Artist]")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Song by ${song.artist}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC084FC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Two Action Buttons Row: [Translate] & [Get Song Meaning]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Translate Button (Only says "Translate")
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedLanguage != "Original") SpotifyGreen.copy(alpha = 0.25f)
                                else StormBlackElevated
                            )
                            .border(
                                1.dp,
                                if (selectedLanguage != "Original") SpotifyGreen else StormSlateBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isDropdownExpanded = true
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translate",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Translate",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmoke
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                tint = WhiteSmokeMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (lang == "Original") "Original Language" else lang,
                                        fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                        color = if (lang == selectedLanguage) SpotifyGreen else WhiteSmoke
                                    )
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectLanguage(lang)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Button 2: Get Song Meaning Button (Uses Gemini to get overall song meaning)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (showSongMeaningCard) Color(0xFF8B5CF6).copy(alpha = 0.25f)
                            else StormBlackElevated
                        )
                        .border(
                            1.dp,
                            if (showSongMeaningCard) Color(0xFFC084FC) else StormSlateBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (showSongMeaningCard && !songMeaningExplanation.isNullOrBlank()) {
                                showSongMeaningCard = false
                                onClearSongMeaning()
                            } else {
                                showSongMeaningCard = true
                                onExplainSongMeaning(fullLyricsText)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Get Song Meaning",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Get Song Meaning",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteSmoke
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Plain Text Lyrics Stanzas List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = Triple(isLyricsLoading, stanzas.isEmpty(), stanzas),
                    label = "lyrics_content_transition"
                ) { (loading, empty, stanzasList) ->
                    if (loading && empty) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    } else if (empty) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lyrics aren't available for this song.",
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Optional Overall Song Meaning Card if requested via Gemini
                            if (showSongMeaningCard) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF1E1B4B))
                                            .border(1.dp, Color(0xFFC084FC), RoundedCornerShape(16.dp))
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = "Gemini AI Meaning",
                                                        tint = Color(0xFFC084FC),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "GEMINI AI SONG MEANING",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFFC084FC),
                                                        letterSpacing = 0.8.sp
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        showSongMeaningCard = false
                                                        onClearSongMeaning()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        tint = WhiteSmokeMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (isExplainingSongMeaning) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = Color(0xFFC084FC),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "Analyzing overall song meaning with Gemini AI...",
                                                        fontSize = 12.5.sp,
                                                        color = WhiteSmokeMuted
                                                    )
                                                }
                                            } else if (!songMeaningExplanation.isNullOrBlank()) {
                                                Text(
                                                    text = songMeaningExplanation,
                                                    fontSize = 14.sp,
                                                    color = WhiteSmoke,
                                                    lineHeight = 20.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Plain text stanzas
                            itemsIndexed(stanzasList, key = { index, stanza -> "${stanza.hashCode()}_$index" }) { index, stanza ->
                            val isSelected = selectedStanzaText == stanza

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (isSelected) {
                                            onClearStanzaExplanation()
                                        } else {
                                            onExplainStanza(stanza)
                                        }
                                    }
                            ) {
                                Text(
                                    text = stanza,
                                    fontSize = 17.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SpotifyGreen else WhiteSmoke,
                                    lineHeight = 26.sp
                                )

                                // Individual stanza AI interpretation if tapped
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF151828))
                                            .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lightbulb,
                                                        contentDescription = "AI Interpretation",
                                                        tint = Color(0xFFFBBF24),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "GEMINI AI STANZA ANALYSIS",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFFFBBF24),
                                                        letterSpacing = 0.6.sp
                                                    )
                                                }

                                                IconButton(
                                                    onClick = onClearStanzaExplanation,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close Explanation",
                                                        tint = WhiteSmokeMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (isExplainingStanza) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = Color(0xFFC084FC),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "Analyzing stanza meaning with Gemini...",
                                                        fontSize = 12.5.sp,
                                                        color = WhiteSmokeMuted
                                                    )
                                                }
                                            } else if (!selectedStanzaExplanation.isNullOrBlank()) {
                                                Text(
                                                    text = selectedStanzaExplanation,
                                                    fontSize = 13.5.sp,
                                                    color = WhiteSmoke,
                                                    lineHeight = 19.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Metadata Listing: Songwriters, Publisher, Date Published, Source Attribution
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(StormBlackElevated.copy(alpha = 0.6f))
                                    .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "SONG CREDITS & METADATA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFC084FC),
                                    letterSpacing = 1.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(StormSlateBorder.copy(alpha = 0.5f))
                                )

                                // Songwriters
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "Songwriters: ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WhiteSmoke,
                                        modifier = Modifier.width(115.dp)
                                    )
                                    Text(
                                        text = metadata.songwriters,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = WhiteSmokeMuted,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Publisher
                                if (metadata.publisher.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "Publisher: ",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WhiteSmoke,
                                            modifier = Modifier.width(115.dp)
                                        )
                                        Text(
                                            text = metadata.publisher,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = WhiteSmokeMuted,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // Date Published
                                if (metadata.publishDate.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "Date Published: ",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WhiteSmoke,
                                            modifier = Modifier.width(115.dp)
                                        )
                                        Text(
                                            text = metadata.publishDate,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = WhiteSmokeMuted,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // Source (place from where it is taken)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "Source: ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WhiteSmoke,
                                        modifier = Modifier.width(115.dp)
                                    )
                                    Text(
                                        text = metadata.source,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SpotifyGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Lyrics provided in plain text format for personal and educational use.",
                                fontSize = 11.5.sp,
                                color = WhiteSmokeMuted.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                        }
                    }
                }
                }
            }

            // Bottom Player Control Bar
            var isLyricsScrubbing by remember { mutableStateOf(false) }
            var lyricsScrubRatio by remember { mutableFloatStateOf(0f) }

            val effectiveDuration = if (durationMs > 0) durationMs else 30000L
            val currentProgRatio = if (effectiveDuration > 0) (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f) else 0f
            val displayLyricsRatio = if (isLyricsScrubbing) lyricsScrubRatio else currentProgRatio
            val displayLyricsPosMs = if (isLyricsScrubbing) (lyricsScrubRatio * effectiveDuration).toLong() else currentPositionMs

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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

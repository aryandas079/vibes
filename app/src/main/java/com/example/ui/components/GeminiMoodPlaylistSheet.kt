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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GeminiMoodPlaylist
import com.example.model.GeminiTrackSequence
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.liquidGlassEffect
import com.example.ui.viewmodel.MoodPlaylistUiState

data class MoodPreset(val label: String, val icon: ImageVector, val accentColor: Color)
data class ActivityPreset(val label: String, val icon: ImageVector)

val MOOD_PRESETS = listOf(
    MoodPreset("Energetic & Hyped", Icons.Default.ElectricBolt, Color(0xFFF59E0B)),
    MoodPreset("Deep Focus & Flow", Icons.Default.SelfImprovement, Color(0xFF06B6D4)),
    MoodPreset("Late Night Melancholy", Icons.Default.Nightlight, Color(0xFF8B5CF6)),
    MoodPreset("Chill Sunset", Icons.Default.Spa, Color(0xFFEC4899)),
    MoodPreset("Beast Mode Gym", Icons.Default.LocalFireDepartment, Color(0xFFEF4444)),
    MoodPreset("Euphoric Dance", Icons.Default.TrendingUp, Color(0xFF10B981)),
    MoodPreset("Romantic & Intimate", Icons.Default.Favorite, Color(0xFFF43F5E))
)

val ACTIVITY_PRESETS = listOf(
    ActivityPreset("Night Highway Drive", Icons.Default.DirectionsSubway),
    ActivityPreset("Deep Coding", Icons.Default.Laptop),
    ActivityPreset("Gym Workout", Icons.Default.DirectionsRun),
    ActivityPreset("Study Session", Icons.Default.Headphones),
    ActivityPreset("Unwind & Relax", Icons.Default.Spa),
    ActivityPreset("City Walking", Icons.Default.Speed)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiMoodPlaylistSheet(
    uiState: MoodPlaylistUiState,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismiss: () -> Unit,
    onGeneratePlaylist: (mood: String, activity: String, customPrompt: String) -> Unit,
    onPlayPlaylistSequence: (GeminiMoodPlaylist) -> Unit,
    onPlayTrack: (Song) -> Unit,
    onSaveToLibrary: (GeminiMoodPlaylist) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    var selectedMood by remember { mutableStateOf(MOOD_PRESETS[0].label) }
    var selectedActivity by remember { mutableStateOf(ACTIVITY_PRESETS[0].label) }
    var customPrompt by remember { mutableStateOf("") }
    var isSavedToLibrary by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "gemini_sheet_spin")
    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_spin"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StormBlackBg,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("gemini_mood_playlist_sheet")
        ) {
            // Drag handle & Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Mood & Activity Sequencer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5F5F7),
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "Custom track sequences tuned to your pace and vibe",
                            fontSize = 11.5.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            when (uiState) {
                is MoodPlaylistUiState.Generating -> {
                    // Loading State with real-time steps
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.12f))
                                    .border(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = Color(0xFFC084FC),
                                    strokeWidth = 3.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = uiState.step,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF5F5F7),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Calculating BPM curves, harmonic key transitions, and track energy levels...",
                                fontSize = 12.sp,
                                color = Color(0xFF71717A),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }

                is MoodPlaylistUiState.Success -> {
                    // Generated Sequenced Playlist View
                    val playlist = uiState.playlist
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header summary card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF2E1065).copy(alpha = 0.7f),
                                                StormBlackElevated
                                            )
                                        )
                                    )
                                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                    .padding(18.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFC084FC).copy(alpha = 0.25f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = playlist.mood.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE9D5FF)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SpotifyGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = playlist.activity.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpotifyGreen
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${playlist.tracks.size} TRACKS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = playlist.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFF5F5F7)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = playlist.narrativeArc,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        color = Color(0xFFD4D4D8)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action buttons: Play All & Save to Library
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                                onPlayPlaylistSequence(playlist)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("play_mood_playlist_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SpotifyGreen,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Play Sequence",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                                onSaveToLibrary(playlist)
                                                isSavedToLibrary = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSavedToLibrary) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f),
                                                contentColor = if (isSavedToLibrary) Color(0xFFC084FC) else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("save_mood_playlist_button")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSavedToLibrary) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (isSavedToLibrary) "Saved" else "Save Playlist",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Curated Track Sequence & Transitions",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF5F5F7),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Track sequence timeline items
                        itemsIndexed(playlist.tracks) { index, track ->
                            TrackSequenceItemCard(
                                track = track,
                                onPlayTrack = { song -> onPlayTrack(song) }
                            )
                        }

                        // Reset button to create another
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    isSavedToLibrary = false
                                    onGeneratePlaylist(selectedMood, selectedActivity, customPrompt)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resequence_mood_playlist_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color(0xFFC084FC)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Regenerate with New Trajectory",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Initial Setup / Prompt Config View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 580.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Mood Selector
                        item {
                            Column {
                                Text(
                                    text = "Current Mood",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F5F7)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(MOOD_PRESETS) { preset ->
                                        val isSelected = selectedMood == preset.label
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) preset.accentColor.copy(alpha = 0.28f)
                                                    else Color.White.copy(alpha = 0.06f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) preset.accentColor else Color.White.copy(alpha = 0.08f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                                    selectedMood = preset.label
                                                }
                                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = preset.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) preset.accentColor else Color(0xFFA1A1AA),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = preset.label,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Activity Selector
                        item {
                            Column {
                                Text(
                                    text = "Activity / Setting",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F5F7)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(ACTIVITY_PRESETS) { activity ->
                                        val isSelected = selectedActivity == activity.label
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) SpotifyGreen.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = 0.06f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                                    selectedActivity = activity.label
                                                }
                                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = activity.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) SpotifyGreen else Color(0xFFA1A1AA),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = activity.label,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 3: Custom Vibe / Freeform prompt
                        item {
                            Column {
                                Text(
                                    text = "Specific Nuance (Optional)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5F5F7)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    BasicTextField(
                                        value = customPrompt,
                                        onValueChange = { customPrompt = it },
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 13.5.sp
                                        ),
                                        cursorBrush = SolidColor(Color(0xFFC084FC)),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (customPrompt.isBlank()) {
                                                Text(
                                                    text = "e.g. Rainy neon streets, synthwave bass, building to 130 BPM...",
                                                    color = Color(0xFF71717A),
                                                    fontSize = 13.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }

                        // Generate CTA Button
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    onGeneratePlaylist(selectedMood, selectedActivity, customPrompt)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("generate_gemini_playlist_cta"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Generate Track Sequence",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackSequenceItemCard(
    track: GeminiTrackSequence,
    onPlayTrack: (Song) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val stageColor = when {
        track.energyStage.contains("Climax", ignoreCase = true) || track.energyStage.contains("Peak", ignoreCase = true) -> Color(0xFFEF4444)
        track.energyStage.contains("Build", ignoreCase = true) -> Color(0xFFF59E0B)
        track.energyStage.contains("Cool", ignoreCase = true) || track.energyStage.contains("Afterglow", ignoreCase = true) -> Color(0xFF06B6D4)
        else -> Color(0xFF8B5CF6)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sequence number badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(stageColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${track.sequenceNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = stageColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Artwork or placeholder
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    if (track.resolvedSong?.artworkUrl != null) {
                        AsyncImage(
                            model = track.resolvedSong.artworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFF71717A),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F5F7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badges for Energy & Tempo
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(stageColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = track.energyStage.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = stageColor
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = track.tempo,
                        fontSize = 10.sp,
                        color = Color(0xFF71717A)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play Preview button
                if (track.resolvedSong != null) {
                    IconButton(
                        onClick = { onPlayTrack(track.resolvedSong) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play track",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transition rationale card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "Flow: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC084FC)
                    )
                    Text(
                        text = track.transitionReason,
                        fontSize = 11.sp,
                        color = Color(0xFFD4D4D8),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

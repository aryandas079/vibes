package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormBlackSurface
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect
import com.example.util.HummingMatchCandidate
import com.example.util.HummingMatchResult
import com.example.util.HummingSearchService
import com.example.util.HummingSearchState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HummingSearchSheet(
    sheetState: SheetState,
    catalogSongs: List<Song>,
    onDismiss: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onSearchSong: (String) -> Unit,
    onSwitchToVoice: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hummingService = remember { HummingSearchService() }
    val state by hummingService.state.collectAsState()
    val audioAmplitude by hummingService.audioAmplitude.collectAsState()

    var secondsRecorded by remember { mutableIntStateOf(0) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            coroutineScope.launch {
                hummingService.startHummingRecognition(catalogSongs) { sec ->
                    secondsRecorded = sec
                }
            }
        }
    }

    fun startHummingFlow() {
        if (hasAudioPermission) {
            coroutineScope.launch {
                secondsRecorded = 0
                hummingService.startHummingRecognition(catalogSongs) { sec ->
                    secondsRecorded = sec
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        startHummingFlow()
    }

    DisposableEffect(Unit) {
        onDispose {
            hummingService.reset()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hummingAura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StormBlackSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WhiteSmokeMuted.copy(alpha = 0.4f))
            )
        },
        modifier = modifier.testTag("humming_search_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6C5CE7).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFFA29BFE),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Search with Humming",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteSmoke
                        )
                        Text(
                            text = "Hum, whistle, or sing a tune",
                            fontSize = 11.5.sp,
                            color = WhiteSmokeMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = WhiteSmokeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector Row: Voice vs Humming
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StormBlackElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .clickable { onSwitchToVoice?.invoke() }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = WhiteSmokeMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Search", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = WhiteSmokeMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpotifyGreen.copy(alpha = 0.25f))
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Humming Search", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = WhiteSmoke)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (val currentState = state) {
                is HummingSearchState.Recording -> {
                    // Recording Active View
                    HummingRecordingView(
                        secondsRecorded = secondsRecorded,
                        maxSeconds = currentState.maxSeconds,
                        audioAmplitude = audioAmplitude,
                        pulseScale = pulseScale,
                        onStopEarly = { hummingService.stopRecordingEarly() }
                    )
                }

                is HummingSearchState.Analyzing -> {
                    // Analyzing View
                    HummingAnalyzingView()
                }

                is HummingSearchState.Success -> {
                    // Result View
                    HummingResultView(
                        result = currentState.result,
                        onPlaySong = onPlaySong,
                        onSearchSong = onSearchSong,
                        onHumAgain = { startHummingFlow() }
                    )
                }

                is HummingSearchState.Error -> {
                    // Error View
                    HummingErrorView(
                        message = currentState.message,
                        onRetry = { startHummingFlow() },
                        onSimulate = {
                            coroutineScope.launch {
                                hummingService.simulateHummingMatch(catalogSongs)
                            }
                        }
                    )
                }

                is HummingSearchState.Idle -> {
                    Button(
                        onClick = { startHummingFlow() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C5CE7),
                            contentColor = WhiteSmoke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Humming")
                    }
                }
            }
        }
    }
}

@Composable
private fun HummingRecordingView(
    secondsRecorded: Int,
    maxSeconds: Int,
    audioAmplitude: Float,
    pulseScale: Float,
    onStopEarly: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Melodic Aura Visualizer
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing background rings
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale + (audioAmplitude * 0.45f))
                    .clip(CircleShape)
                    .background(Color(0xFF6C5CE7).copy(alpha = 0.14f))
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(1f + (audioAmplitude * 0.35f))
                    .clip(CircleShape)
                    .background(Color(0xFFA29BFE).copy(alpha = 0.22f))
            )

            // Center Music Waveform Disc
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6C5CE7), Color(0xFF4834D4))
                        )
                    )
                    .border(2.dp, WhiteSmoke.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Humming",
                    tint = WhiteSmoke,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Listening to your melody...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Hum or whistle the tune clearly for ~5 seconds",
            fontSize = 13.sp,
            color = WhiteSmokeMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar for 7s recording
        val progress = (secondsRecorded.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0:0$secondsRecorded",
                    fontSize = 12.sp,
                    color = Color(0xFFA29BFE),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "0:0$maxSeconds",
                    fontSize = 12.sp,
                    color = WhiteSmokeMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFA29BFE),
                trackColor = StormBlackElevated
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stop & Identify Button
        Button(
            onClick = onStopEarly,
            colors = ButtonDefaults.buttonColors(
                containerColor = StormBlackElevated,
                contentColor = WhiteSmoke
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .border(1.dp, StormSlateBorder, RoundedCornerShape(12.dp))
                .height(46.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                tint = Color(0xFFA29BFE),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Identify Melody Now",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HummingAnalyzingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(76.dp),
                color = Color(0xFFA29BFE),
                strokeWidth = 3.5.dp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFA29BFE),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Analyzing Melodic Contour...",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comparing vocal frequencies and harmonic hooks to catalog melodies...",
            fontSize = 13.sp,
            color = WhiteSmokeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun HummingResultView(
    result: HummingMatchResult,
    onPlaySong: (Song) -> Unit,
    onSearchSong: (String) -> Unit,
    onHumAgain: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // High confidence badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF223A25))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${result.matchConfidence}% MATCH FOUND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF7DE68D)
                    )
                }
            }

            TextButton(onClick = onHumAgain) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFFA29BFE),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hum Again",
                    fontSize = 12.sp,
                    color = Color(0xFFA29BFE)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Matched Song Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 6.dp)
                .border(1.dp, WhiteSmoke.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Album Art
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, WhiteSmoke.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    MusicaImage(
                        model = result.song.artworkUrl,
                        contentDescription = result.song.title,
                        titlePlaceholder = result.song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.song.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = result.song.artist,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WhiteSmokeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.song.genre} • ${result.song.releaseYear}",
                        fontSize = 11.5.sp,
                        color = WhiteSmokeSoft
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Melodic Hook Description Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StormBlackElevated)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = Color(0xFFA29BFE),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.melodyHookDescription,
                    fontSize = 12.sp,
                    color = WhiteSmokeMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Play Now Button
        Button(
            onClick = { onPlaySong(result.song) },
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotifyGreen,
                contentColor = Color(0xFF141414)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("humming_play_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Play Matched Song Now",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // View In Search Button
        OutlinedButton(
            onClick = { onSearchSong(result.song.title) },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WhiteSmoke
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(listOf(StormSlateBorder, StormSlateBorder))
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "View in Music Search Catalog",
                fontSize = 13.sp
            )
        }

        // Alternative Candidates
        if (result.alternativeMatches.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Other Close Matches:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = WhiteSmokeSoft,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            result.alternativeMatches.forEach { candidate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StormBlackElevated.copy(alpha = 0.6f))
                        .clickable { onPlaySong(candidate.song) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${candidate.song.title} - ${candidate.song.artist}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WhiteSmoke,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = candidate.reason,
                            fontSize = 11.sp,
                            color = WhiteSmokeMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2C3246))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${candidate.matchConfidence}%",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA29BFE)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HummingErrorView(
    message: String,
    onRetry: () -> Unit,
    onSimulate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Could not identify melody",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFFFFB4AB),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StormBlackElevated,
                    contentColor = WhiteSmoke
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Try Again", fontSize = 12.sp)
            }

            Button(
                onClick = onSimulate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C5CE7),
                    contentColor = WhiteSmoke
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Hum", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

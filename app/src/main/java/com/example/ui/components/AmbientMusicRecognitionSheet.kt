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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.util.AmbientMatchResult
import com.example.util.AmbientMusicRecognizer
import com.example.util.AmbientRecognitionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientMusicRecognitionSheet(
    sheetState: SheetState,
    catalogSongs: List<Song>,
    onDismiss: () -> Unit,
    onPlaySong: (Song) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val recognizer = remember { AmbientMusicRecognizer() }
    val state by recognizer.state.collectAsState()
    val audioAmplitude by recognizer.audioAmplitude.collectAsState()

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
                recognizer.startAmbientRecognition(catalogSongs) { sec ->
                    secondsRecorded = sec
                }
            }
        }
    }

    fun startRecognitionFlow() {
        if (hasAudioPermission) {
            coroutineScope.launch {
                secondsRecorded = 0
                recognizer.startAmbientRecognition(catalogSongs) { sec ->
                    secondsRecorded = sec
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        startRecognitionFlow()
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizer.reset()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambientRipple")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
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
        modifier = modifier.testTag("ambient_recognition_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title Area
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
                            .background(SpotifyGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Music Recognition",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteSmoke
                        )
                        Text(
                            text = "Ambient Song Finder & Lyrics Sync",
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

            Spacer(modifier = Modifier.height(24.dp))

            when (val currentState = state) {
                is AmbientRecognitionState.Listening -> {
                    AmbientListeningView(
                        secondsRecorded = secondsRecorded,
                        maxSeconds = currentState.maxSeconds,
                        audioAmplitude = audioAmplitude,
                        pulseScale = pulseScale,
                        onStopEarly = { recognizer.stopRecognition() }
                    )
                }

                is AmbientRecognitionState.Analyzing -> {
                    AmbientAnalyzingView()
                }

                is AmbientRecognitionState.Success -> {
                    AmbientSuccessView(
                        result = currentState.result,
                        onPlaySong = {
                            onPlaySong(it)
                            onDismiss()
                        },
                        onOpenLyrics = {
                            onOpenSongDetails(it)
                            onDismiss()
                        },
                        onScanAgain = { startRecognitionFlow() }
                    )
                }

                is AmbientRecognitionState.Error -> {
                    AmbientErrorView(
                        message = currentState.message,
                        onRetry = { startRecognitionFlow() }
                    )
                }

                is AmbientRecognitionState.Idle -> {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            }
        }
    }
}

@Composable
private fun AmbientListeningView(
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
        // Glowing Concentric Pulse Orbs matching premium acoustic design
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(155.dp)
                    .scale(pulseScale + (audioAmplitude * 0.5f))
                    .clip(CircleShape)
                    .background(SpotifyGreen.copy(alpha = 0.12f))
            )

            Box(
                modifier = Modifier
                    .size(125.dp)
                    .scale(1f + (audioAmplitude * 0.4f))
                    .clip(CircleShape)
                    .background(SpotifyGreen.copy(alpha = 0.2f))
            )

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(SpotifyGreen, Color(0xFF0F5A26))
                        )
                    )
                    .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                    .clickable { onStopEarly() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Listening",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Listening for ambient audio...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Hold microphone close to speakers or environment music source",
            fontSize = 12.5.sp,
            color = WhiteSmokeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Ambient capture progress bar
        val progress = (secondsRecorded.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Capturing: 0:0$secondsRecorded",
                    fontSize = 11.5.sp,
                    color = SpotifyGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "0:0$maxSeconds",
                    fontSize = 11.5.sp,
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
                color = SpotifyGreen,
                trackColor = StormBlackElevated
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Identify Song Now",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AmbientAnalyzingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(76.dp),
                color = SpotifyGreen,
                strokeWidth = 3.5.dp
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Analyzing Ambient Fingerprint...",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comparing acoustic spectral characteristics to verify artist and sync lyrics...",
            fontSize = 13.sp,
            color = WhiteSmokeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun AmbientSuccessView(
    result: AmbientMatchResult,
    onPlaySong: (Song) -> Unit,
    onOpenLyrics: (Song) -> Unit,
    onScanAgain: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Recognition Match Badge & Trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B3B22))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${result.matchConfidence}% SPECTRAL MATCH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5FF37D)
                )
            }

            TextButton(onClick = onScanAgain) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Scan Again",
                    fontSize = 12.sp,
                    color = SpotifyGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recognized Song Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    MusicaImage(
                        model = result.song.artworkUrl,
                        contentDescription = result.song.title,
                        titlePlaceholder = result.song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.song.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = result.song.artist,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = WhiteSmokeMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.song.genre} • Confidence: ${result.matchConfidence}%",
                        fontSize = 11.sp,
                        color = WhiteSmokeSoft
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SPECTRAL FINGERPRINT NOTE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(StormBlackElevated)
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = result.spectralDetails,
                    fontSize = 11.5.sp,
                    color = WhiteSmokeMuted,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RICH LYRICS SECTION (Direct integration inside the bottom sheet)
        Text(
            text = "Available Synced Lyrics:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SpotifyGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(StormBlackElevated.copy(alpha = 0.5f))
                .border(1.dp, StormSlateBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (result.lyricsPreview.isNotBlank()) {
                    Text(
                        text = result.lyricsPreview,
                        fontSize = 13.sp,
                        color = WhiteSmokeSoft,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = null,
                            tint = WhiteSmokeMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No offline lyrics found for ambient match.",
                            fontSize = 12.sp,
                            color = WhiteSmokeMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trigger Play / View Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onPlaySong(result.song) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreen,
                    contentColor = Color(0xFF141414)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play Song",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { onOpenLyrics(result.song) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WhiteSmoke
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(listOf(StormSlateBorder, StormSlateBorder))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lyrics,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Full Synced Lyrics",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AmbientErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unable to recognize environment music",
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
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotifyGreen,
                contentColor = Color(0xFF141414)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Scanning Again", fontWeight = FontWeight.Bold)
        }
    }
}

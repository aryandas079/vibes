package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormBlackSurface
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect
import com.example.util.VoiceSearchHelper
import com.example.util.VoiceSearchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSearchSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onQueryRecognized: (String) -> Unit,
    onSwitchToHumming: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceHelper = remember { VoiceSearchHelper(context) }
    val state by voiceHelper.state.collectAsState()
    val rmsAmplitude by voiceHelper.rmsAmplitude.collectAsState()

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
            voiceHelper.startListening()
        }
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            voiceHelper.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    // Auto navigate / trigger search upon success
    LaunchedEffect(state) {
        if (state is VoiceSearchState.Success) {
            val query = (state as VoiceSearchState.Success).recognizedText
            onQueryRecognized(query)
            onDismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val sampleVoiceQueries = listOf(
        "Cruel Summer", "The Weeknd", "Espresso",
        "Billie Eilish", "Coldplay", "Die With A Smile",
        "Taylor Swift", "Blinding Lights"
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
        modifier = modifier.testTag("voice_search_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice Search",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke
                    )
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
                        .background(SpotifyGreen.copy(alpha = 0.25f))
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = SpotifyGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Search", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = WhiteSmoke)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                        .clickable { onSwitchToHumming?.invoke() }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = WhiteSmokeMuted, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Humming Search", fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = WhiteSmokeMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Microphone Visualizer
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                val isListening = state is VoiceSearchState.Listening || state is VoiceSearchState.Processing

                if (isListening) {
                    // Outer pulsating aura ring
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale + (rmsAmplitude * 0.4f))
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.12f))
                    )

                    // Secondary dynamic amplitude ring
                    Box(
                        modifier = Modifier
                            .size(114.dp)
                            .scale(1f + (rmsAmplitude * 0.35f))
                            .clip(CircleShape)
                            .background(SpotifyGreen.copy(alpha = 0.25f))
                    )
                }

                // Center microphone button
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (isListening) SpotifyGreen else StormBlackElevated,
                                    if (isListening) Color(0xFF137533) else StormBlackCard
                                )
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) WhiteSmoke else StormSlateBorder,
                            CircleShape
                        )
                        .clickable {
                            if (hasAudioPermission) {
                                voiceHelper.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("voice_search_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isListening) WhiteSmoke else WhiteSmokeMuted,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Status message
            when (val currentState = state) {
                is VoiceSearchState.Listening -> {
                    Text(
                        text = "Listening...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Say an artist, song title, or lyrics",
                        fontSize = 13.5.sp,
                        color = WhiteSmokeMuted,
                        textAlign = TextAlign.Center
                    )
                }

                is VoiceSearchState.Processing -> {
                    Text(
                        text = currentState.partialText.ifBlank { "Processing voice..." },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transcribing audio...",
                        fontSize = 13.sp,
                        color = WhiteSmokeMuted
                    )
                }

                is VoiceSearchState.Success -> {
                    Text(
                        text = "\"${currentState.recognizedText}\"",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Searching catalog...",
                        fontSize = 13.sp,
                        color = SpotifyGreen
                    )
                }

                 is VoiceSearchState.Error -> {
                    Text(
                        text = currentState.message,
                        fontSize = 13.sp,
                        color = Color(0xFFFFB4AB),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { voiceHelper.startListening() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StormBlackElevated,
                                contentColor = WhiteSmoke
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Try Mic", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { voiceHelper.simulateVoiceMatch("Cruel Summer") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Simulate Voice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is VoiceSearchState.Idle -> {
                    Text(
                        text = "Tap the microphone to speak",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WhiteSmoke
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Voice Suggestions / Emulation Pills
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Or tap a popular voice search:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteSmokeSoft,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleVoiceQueries) { query ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(StormBlackElevated)
                                .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
                                .clickable {
                                    onQueryRecognized(query)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("voice_suggestion_chip")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = query,
                                    fontSize = 12.5.sp,
                                    color = WhiteSmoke,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

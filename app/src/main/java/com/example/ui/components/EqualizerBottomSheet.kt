package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    playerManager: AudioPlayerManager,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    var isEnabled by remember { mutableStateOf(playerManager.isEqualizerEnabled) }
    val numBands = playerManager.getNumberOfBands().toInt().coerceIn(1, 10)
    val range = playerManager.getBandLevelRange()
    val minLevel = range.getOrNull(0)?.toFloat() ?: -1500f
    val maxLevel = range.getOrNull(1)?.toFloat() ?: 1500f

    // Band levels state
    val bandLevels = remember {
        mutableStateMapOf<Int, Float>().apply {
            for (i in 0 until numBands) {
                this[i] = playerManager.getBandLevel(i.toShort()).toFloat()
            }
        }
    }

    val presets = remember {
        try {
            playerManager.getPresetNames()
        } catch (e: Exception) {
            listOf("Flat", "Rock", "Pop", "Jazz", "Classical", "Bass Boost")
        }
    }

    var selectedPreset by remember { mutableStateOf("Custom") }

    // Animated visualizer spectrum wave
    val infiniteTransition = rememberInfiniteTransition(label = "EqualizerVisualizer")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F0E17),
        contentColor = Color(0xFFF5F5F7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Equalizer",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Audio Equalizer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5F5F7)
                        )
                        Text(
                            text = "Frequency Spectrum & Faders",
                            fontSize = 12.sp,
                            color = Color(0xFF8B8F9F)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        isEnabled = it
                        playerManager.isEqualizerEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFFFFF),
                        checkedTrackColor = Color(0xFF8B5CF6),
                        uncheckedThumbColor = Color(0xFF8B8F9F),
                        uncheckedTrackColor = Color(0xFF1E1B32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-Time Animated Visualizer Spectrum Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF171527))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val path = Path()
                    path.moveTo(0f, height / 2f)

                    val waveSteps = 40
                    val stepX = width / waveSteps
                    for (i in 0..waveSteps) {
                        val x = i * stepX
                        val angle = (x / width * 360f + phase) * (Math.PI / 180f)
                        val boostMultiplier = if (isEnabled) 1.2f else 0.2f
                        val y = (height / 2f) + (sin(angle * 2) * (height * 0.35f) * boostMultiplier).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEC4899))
                        ),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }

                Text(
                    text = if (isEnabled) "ACTIVE SPECTRUM" else "EQUALIZER BYPASSED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B8F9F).copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(presets) { index, presetName ->
                    val isSelected = selectedPreset == presetName
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedPreset = presetName
                                try {
                                    playerManager.usePreset(index.toShort())
                                    for (b in 0 until numBands) {
                                        bandLevels[b] = playerManager.getBandLevel(b.toShort()).toFloat()
                                    }
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            },
                        color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1B32),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = presetName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF8B8F9F),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frequency Sliders / Faders Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (b in 0 until numBands) {
                    val currentVal = bandLevels[b] ?: 0f
                    val centerFreqHz = playerManager.getCenterFreq(b.toShort())
                    val freqLabel = if (centerFreqHz >= 1000) "${centerFreqHz / 1000}k" else "${centerFreqHz}"

                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // dB Gain Text
                        Text(
                            text = "${(currentVal / 100).toInt()}dB",
                            fontSize = 10.sp,
                            color = if (isEnabled) Color(0xFFC084FC) else Color(0xFF8B8F9F)
                        )

                        // Vertical Slider representation using Box and Slider with rotation or custom column layout
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Custom vertical slider slider
                            androidx.compose.material3.Slider(
                                value = currentVal,
                                onValueChange = { newVal ->
                                    bandLevels[b] = newVal
                                    selectedPreset = "Custom"
                                    playerManager.setBandLevel(b.toShort(), newVal.toInt().toShort())
                                },
                                valueRange = minLevel..maxLevel,
                                enabled = isEnabled,
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = -90f
                                        translationX = 0f
                                        translationY = 0f
                                    }
                                    .width(160.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF06B6D4),
                                    activeTrackColor = Color(0xFF8B5CF6),
                                    inactiveTrackColor = Color(0xFF252238)
                                )
                            )
                        }

                        // Frequency Label
                        Text(
                            text = freqLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5F5F7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text(
                    text = "Apply & Close",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
 * Premium Search Bar with fully functioning Google Voice Search,
 * sleek liquid glass aesthetic, and focus/click events for search history dropdown.
 */
@Composable
fun SearchableTopBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<Song> = emptyList(),
    isSearching: Boolean = false,
    onPlaySong: (Song) -> Unit = {},
    onOpenSongDetails: (Song) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "Search songs, artists, albums..."
) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // Google Voice Search Activity Launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                VibesHaptics.success(context, view)
                onQueryChanged(spokenText.trim())
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassEffect(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
                .background(StormBlackElevated.copy(alpha = 0.85f))
                .border(1.2.dp, if (query.isNotBlank()) SpotifyGreen.copy(alpha = 0.5f) else StormSlateBorder, RoundedCornerShape(24.dp))
                .clickable(interactionSource = interactionSource, indication = null) {
                    VibesHaptics.mediumClick(context, view)
                    onClick()
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Leading Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (query.isNotBlank()) SpotifyGreen else WhiteSmokeMuted,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Text Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.5.sp,
                            color = WhiteSmokeMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = WhiteSmoke,
                            fontWeight = FontWeight.Medium
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            VibesHaptics.strongClick(context, view)
                            focusManager.clearFocus()
                        }),
                        cursorBrush = SolidColor(SpotifyGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                onFocusChanged(focusState.isFocused)
                                if (focusState.isFocused) {
                                    onClick()
                                }
                            }
                            .testTag("searchable_topbar_input")
                    )
                }

                // Loading Spinner if searching
                if (isSearching) {
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SpotifyGreen,
                        strokeWidth = 2.dp
                    )
                }

                // Clear Search Query button (Cross 'X')
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = {
                            VibesHaptics.mediumClick(context, view)
                            onQueryChanged("")
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = WhiteSmokeMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Google Voice Search Mic Button (100% Fully Working)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF4285F4).copy(alpha = 0.25f), // Google Blue
                                    Color(0xFFEA4335).copy(alpha = 0.25f), // Google Red
                                    Color(0xFFFBBC05).copy(alpha = 0.25f), // Google Yellow
                                    Color(0xFF34A853).copy(alpha = 0.25f)  // Google Green
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF4285F4),
                                    Color(0xFFEA4335),
                                    Color(0xFFFBBC05),
                                    Color(0xFF34A853)
                                )
                            ),
                            CircleShape
                        )
                        .clickable {
                            VibesHaptics.strongClick(context, view)
                            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_PROMPT,
                                    "Speak song or artist name to search..."
                                )
                            }
                            try {
                                voiceSearchLauncher.launch(speechIntent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Google Voice Search is not available on this device",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Google Voice Search",
                        tint = WhiteSmoke,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

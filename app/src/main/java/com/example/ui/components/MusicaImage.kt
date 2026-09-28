package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

@Composable
fun MusicaImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    titlePlaceholder: String = ""
) {
    val modelToUse = remember(model) {
        if (model == null || (model is String && model.isBlank())) {
            "https://invalid-url-to-trigger-placeholder.com/none.jpg"
        } else {
            model
        }
    }

    SubcomposeAsyncImage(
        model = modelToUse,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF1DB954),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            // Gorgeous, colored gradient placeholder with a music note or first letter
            val firstChar = titlePlaceholder.trim().firstOrNull()?.uppercase() ?: ""
            val gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFFEC4899), // Pink
                    Color(0xFF3B82F6)  // Blue
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                if (firstChar.isNotEmpty() && firstChar.first().isLetterOrDigit()) {
                    Text(
                        text = firstChar,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Placeholder",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    )
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

fun resolveHighResArtworkUrl(titleOrArtist: String): String? {
    return null
}

fun getAbbreviation(text: String): String {
    if (text.isBlank()) return "M"
    // Remove text in parentheses or brackets (e.g., "(feat. ...)")
    val cleanText = text.replace(Regex("\\(.*?\\)|\\[.*?\\]"), "").trim()
    val words = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
    
    return when {
        words.isEmpty() -> "M"
        words.size == 1 -> {
            val word = words[0]
            if (word.length >= 2) word.take(2).uppercase() else word.uppercase()
        }
        words.size == 2 -> {
            (words[0].take(1) + words[1].take(1)).uppercase()
        }
        else -> {
            // E.g., "Shape Of You" -> "SOY"
            (words[0].take(1) + words[1].take(1) + words[2].take(1)).uppercase()
        }
    }
}

@Composable
fun MusicaImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    titlePlaceholder: String = ""
) {
    val context = LocalContext.current
    val effectivePlaceholder = remember(titlePlaceholder, contentDescription) {
        titlePlaceholder.ifBlank { contentDescription ?: "" }
    }

    val finalImageUrl = remember(model, effectivePlaceholder) {
        val raw = when (model) {
            is String -> model.trim()
            else -> ""
        }

        when {
            raw.isNotBlank() && !raw.contains("placeholder") -> {
                if (raw.contains("mzstatic.com")) {
                    raw.replace("http://", "https://")
                        .replace(Regex("\\d+x\\d+bb?\\.(jpg|png)"), "600x600bb.jpg")
                } else if (raw.contains("dzcdn.net/images/artist")) {
                     raw.replace("http://", "https://")
                        .replace(Regex("\\d+x\\d+-000000-80-0-0\\.(jpg|png)"), "500x500-000000-80-0-0.jpg")
                } else if (raw.contains("dzcdn.net/images/cover")) {
                     raw.replace("http://", "https://")
                        .replace(Regex("\\d+x\\d+-000000-80-0-0\\.(jpg|png)"), "500x500-000000-80-0-0.jpg")
                } else {
                    raw.replace("http://", "https://")
                }
            }
            else -> "" // Force empty to trigger the error block directly if it's a placeholder
        }
    }

    val request = remember(finalImageUrl) {
        ImageRequest.Builder(context)
            .data(finalImageUrl.ifBlank { null })
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
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
            val abbr = remember(effectivePlaceholder) { getAbbreviation(effectivePlaceholder) }
            val charCode = abbr.firstOrNull()?.code ?: 0
            val gradientList = when (charCode % 5) {
                0 -> listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6))
                1 -> listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6))
                2 -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFEC4899))
                3 -> listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899))
                else -> listOf(Color(0xFF14B8A6), Color(0xFF0284C7), Color(0xFF6366F1))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(colors = gradientList)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = abbr,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    )
}

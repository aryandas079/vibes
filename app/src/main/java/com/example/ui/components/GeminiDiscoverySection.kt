package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MusicaImage
import com.example.model.DiscoveryRecommendation
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect

enum class DiscoverySectionFilter(val label: String) {
    ALL("All Discoveries"),
    SEARCH("From Searches"),
    LISTENED("From Listened")
}

enum class DiscoveryMoodFilter(val label: String, val color: Color) {
    ALL("All Vibes", Color(0xFFC084FC)),
    RELAXING("Relaxing", Color(0xFF8B5CF6)),
    WORKOUT("Workout", Color(0xFFEF4444)),
    FOCUS("Focus", Color(0xFF3B82F6))
}

@Composable
fun GeminiDiscoverySection(
    recommendations: List<DiscoveryRecommendation>,
    isLoading: Boolean,
    onPlaySong: (Song) -> Unit,
    onRefreshDiscovery: () -> Unit,
    onOpenLyrics: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    favoriteSongIds: Set<Long>,
    currentPlayingId: Long?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    // Section filtering: All, From Searches, From Listened
    var selectedFilter by remember { mutableStateOf(DiscoverySectionFilter.ALL) }
    // Mood-Based filtering tag state
    var selectedMood by remember { mutableStateOf(DiscoveryMoodFilter.ALL) }

    val filteredRecommendations = remember(recommendations, selectedFilter, selectedMood) {
        val baseList = when (selectedFilter) {
            DiscoverySectionFilter.ALL -> recommendations
            DiscoverySectionFilter.SEARCH -> recommendations.filter { it.isFromSearch }
            DiscoverySectionFilter.LISTENED -> recommendations.filter { !it.isFromSearch }
        }
        baseList.filter { rec ->
            when (selectedMood) {
                DiscoveryMoodFilter.ALL -> true
                DiscoveryMoodFilter.RELAXING -> {
                    val tag = rec.vibeTag.lowercase()
                    val text = (rec.aiReason + " " + rec.song.title + " " + rec.song.genre).lowercase()
                    tag.contains("relax") || tag.contains("chill") || tag.contains("calm") || tag.contains("soothing") || tag.contains("ambient") || tag.contains("peace") ||
                    text.contains("relax") || text.contains("chill") || text.contains("calm") || text.contains("soothing") || text.contains("ambient") || text.contains("peaceful") ||
                    rec.song.genre.lowercase().contains("classical") || rec.song.genre.lowercase().contains("ambient") || rec.song.genre.lowercase().contains("acoustic")
                }
                DiscoveryMoodFilter.WORKOUT -> {
                    val tag = rec.vibeTag.lowercase()
                    val text = (rec.aiReason + " " + rec.song.title + " " + rec.song.genre).lowercase()
                    tag.contains("workout") || tag.contains("energetic") || tag.contains("hype") || tag.contains("pump") || tag.contains("fast") || tag.contains("heavy") || tag.contains("run") || tag.contains("gym") ||
                    text.contains("workout") || text.contains("energetic") || text.contains("hype") || text.contains("pump") || text.contains("gym") ||
                    rec.song.genre.lowercase().contains("electronic") || rec.song.genre.lowercase().contains("rock") || rec.song.genre.lowercase().contains("dance") || rec.song.genre.lowercase().contains("hip hop")
                }
                DiscoveryMoodFilter.FOCUS -> {
                    val tag = rec.vibeTag.lowercase()
                    val text = (rec.aiReason + " " + rec.song.title + " " + rec.song.genre).lowercase()
                    tag.contains("focus") || tag.contains("concentration") || tag.contains("study") || tag.contains("instrumental") || tag.contains("deep") || tag.contains("soft") || tag.contains("quiet") ||
                    text.contains("focus") || text.contains("study") || text.contains("instrumental") || text.contains("concentrat") ||
                    rec.song.genre.lowercase().contains("lofi") || rec.song.genre.lowercase().contains("ambient") || rec.song.genre.lowercase().contains("jazz") || rec.song.genre.lowercase().contains("classical")
                }
            }
        }
    }

    // Active Card index in swapper deck
    var activeCardIndex by remember { mutableIntStateOf(0) }
    var isSwappingForward by remember { mutableStateOf(true) }

    // Ensure activeCardIndex stays valid within filtered list
    val safeIndex = remember(activeCardIndex, filteredRecommendations.size) {
        if (filteredRecommendations.isEmpty()) 0
        else activeCardIndex.coerceIn(0, filteredRecommendations.lastIndex)
    }

    // Counting counts for badges
    val searchCount = remember(recommendations) { recommendations.count { it.isFromSearch } }
    val listenedCount = remember(recommendations) { recommendations.count { !it.isFromSearch } }

    val infiniteTransition = rememberInfiniteTransition(label = "gemini_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_discovery_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFF06B6D4)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Discovery",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI Discovery",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HISTORY-POWERED",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC084FC)
                            )
                        }
                    }
                    Text(
                        text = "Curated from your searches & listening trajectory",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onRefreshDiscovery,
                modifier = Modifier
                    .size(36.dp)
                    .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                    .testTag("refresh_gemini_discovery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Recommendations",
                    tint = colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (isLoading) Modifier.rotate(rotationAngle) else Modifier)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sectional Filter Chips
        if (recommendations.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All
                SectionFilterChip(
                    label = "All (${recommendations.size})",
                    isSelected = selectedFilter == DiscoverySectionFilter.ALL,
                    icon = Icons.Default.AutoAwesome,
                    activeColor = Color(0xFF8B5CF6),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedFilter = DiscoverySectionFilter.ALL
                        activeCardIndex = 0
                    }
                )

                // From Searches
                SectionFilterChip(
                    label = "From Searches ($searchCount)",
                    isSelected = selectedFilter == DiscoverySectionFilter.SEARCH,
                    icon = Icons.Default.Search,
                    activeColor = Color(0xFF06B6D4),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedFilter = DiscoverySectionFilter.SEARCH
                        activeCardIndex = 0
                    }
                )

                // From Listened
                SectionFilterChip(
                    label = "From Listened ($listenedCount)",
                    isSelected = selectedFilter == DiscoverySectionFilter.LISTENED,
                    icon = Icons.Default.Headphones,
                    activeColor = SpotifyGreen,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedFilter = DiscoverySectionFilter.LISTENED
                        activeCardIndex = 0
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mood-Based Tag Filter Chips Row (Scrollable)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discovery_mood_filters"),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(DiscoveryMoodFilter.values()) { mood ->
                    val isSelected = selectedMood == mood
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) mood.color.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) mood.color else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedMood = mood
                                activeCardIndex = 0
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("mood_filter_${mood.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mood.label,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) mood.color else colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (isLoading && recommendations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 20.dp)
                    .liquidGlassEffect(shape = RoundedCornerShape(22.dp), elevation = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFC084FC),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Analyzing searches & listening preferences...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (recommendations.isEmpty() || filteredRecommendations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
                    .clickable { onRefreshDiscovery() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (recommendations.isEmpty()) "Generate AI Discovery Playlist" else "No matches in this section",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (recommendations.isEmpty())
                            "Tap to analyze your recent searches and listened tracks for custom recommendations."
                        else "Switch to 'All' or tap refresh to update discovery signals.",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
        } else {
            val currentRecommendation = filteredRecommendations[safeIndex]
            val totalCards = filteredRecommendations.size

            // FEATURED CARD-WISE DECK WITH CARDS SWAPPING ANIMATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("gemini_discovery_deck")
            ) {
                // Background Card Shadow to give visual depth of stacked cards
                if (totalCards > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .offset(y = 8.dp, x = 4.dp)
                            .scale(0.97f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(StormBlackElevated.copy(alpha = 0.55f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    )
                }

                // Swappable Animated Card Content
                AnimatedContent(
                    targetState = safeIndex to currentRecommendation,
                    transitionSpec = {
                        if (isSwappingForward) {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> (fullWidth * 0.9f).toInt() },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + scaleIn(
                                initialScale = 0.88f,
                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(220)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -(fullWidth * 0.9f).toInt() },
                                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                                    ) + scaleOut(
                                        targetScale = 0.88f,
                                        animationSpec = tween(260)
                                    ) + fadeOut(animationSpec = tween(180))
                                )
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> -(fullWidth * 0.9f).toInt() },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + scaleIn(
                                initialScale = 0.88f,
                                animationSpec = tween(320, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(220)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> (fullWidth * 0.9f).toInt() },
                                        animationSpec = tween(260, easing = FastOutSlowInEasing)
                                    ) + scaleOut(
                                        targetScale = 0.88f,
                                        animationSpec = tween(260)
                                    ) + fadeOut(animationSpec = tween(180))
                                )
                        }
                    },
                    label = "card_swap_animation"
                ) { (_, item) ->
                    SwappableCardItem(
                        recommendation = item,
                        cardIndex = safeIndex,
                        totalCards = totalCards,
                        isPlaying = currentPlayingId == item.song.id,
                        isFavorite = favoriteSongIds.contains(item.song.id),
                        onPlay = { onPlaySong(item.song) },
                        onOpenLyrics = { onOpenLyrics(item.song) },
                        onToggleFavorite = { onToggleFavorite(item.song) },
                        onSwapNext = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isSwappingForward = true
                            activeCardIndex = if (safeIndex < totalCards - 1) safeIndex + 1 else 0
                        },
                        onSwapPrev = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isSwappingForward = false
                            activeCardIndex = if (safeIndex > 0) safeIndex - 1 else totalCards - 1
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Sectional Cards Preview Row (Horizontal scroll of other cards in section)
            if (totalCards > 1) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "More in this Section (${totalCards})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(filteredRecommendations, key = { index, item -> "${item.song.id}_$index" }) { index, item ->
                            val isSelected = index == safeIndex
                            MiniDiscoveryCard(
                                recommendation = item,
                                isSelected = isSelected,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    isSwappingForward = index >= safeIndex
                                    activeCardIndex = index
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionFilterChip(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.25f)
                else Color.White.copy(alpha = 0.06f)
            )
            .border(
                1.dp,
                if (isSelected) activeColor else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else Color(0xFFA1A1AA),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFA1A1AA)
            )
        }
    }
}

@Composable
private fun SwappableCardItem(
    recommendation: DiscoveryRecommendation,
    cardIndex: Int,
    totalCards: Int,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onOpenLyrics: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSwapNext: () -> Unit,
    onSwapPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val song = recommendation.song

    // Provenance Accent: Cyan for Search, SpotifyGreen for Listened
    val provenanceColor = if (recommendation.isFromSearch) Color(0xFF06B6D4) else SpotifyGreen
    val provenanceIcon = if (recommendation.isFromSearch) Icons.Default.Search else Icons.Default.Headphones
    val provenanceLabel = if (recommendation.isFromSearch) "SEARCH SIGNAL" else "LISTENED SIGNAL"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
            .border(
                1.dp,
                if (isPlaying) provenanceColor.copy(alpha = 0.7f) else StormSlateBorder,
                RoundedCornerShape(24.dp)
            )
            .pointerInput(cardIndex) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag < -60f) onSwapNext()
                        else if (totalDrag > 60f) onSwapPrev()
                    }
                )
            }
            .padding(16.dp)
            .testTag("swappable_discovery_card_${song.id}")
    ) {
        Column {
            // Provenance Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Origin Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(provenanceColor.copy(alpha = 0.18f))
                        .border(1.dp, provenanceColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = provenanceIcon,
                            contentDescription = null,
                            tint = provenanceColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (recommendation.sourceTitle.isNotBlank())
                                "$provenanceLabel • ${recommendation.sourceTitle}"
                            else recommendation.sourceContext,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = provenanceColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Card counter indicator (e.g. 1/6)
                Text(
                    text = "CARD ${cardIndex + 1} OF $totalCards",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA1A1AA)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Artwork with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                MusicaImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    titlePlaceholder = song.title
                )

                // Match Percentage
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StormBlackBg.copy(alpha = 0.8f))
                        .border(1.dp, StormSlateBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${recommendation.matchPercentage}% MATCH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF5F5F7)
                        )
                    }
                }

                // Vibe Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StormBlackBg.copy(alpha = 0.8f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = recommendation.vibeTag,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke
                    )
                }

                // Play / Equalizer Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) SpotifyGreen else Color.Black.copy(alpha = 0.75f))
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Playing" else "Play",
                        tint = if (isPlaying) Color.Black else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Track title & artist
            Text(
                text = song.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI Reasoning Box (Specific to Search or Listened context)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = recommendation.aiReason,
                        fontSize = 12.sp,
                        color = Color(0xFFE4E4E7),
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Footer: Card Swapping Controls, Lyrics, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Swap Next Card Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.22f))
                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onSwapNext)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("swap_card_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Card",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Swap Card",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC084FC)
                        )
                    }
                }

                // Previous & Next Arrow buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSwapPrev,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous Card",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Card Dots indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (i in 0 until totalCards) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == cardIndex) 6.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == cardIndex) provenanceColor
                                        else Color.White.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }

                    IconButton(
                        onClick = onSwapNext,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next Card",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Right actions: Lyrics & Favorite
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenLyrics,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Synced Lyrics",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) WhiteSmoke else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniDiscoveryCard(
    recommendation: DiscoveryRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (recommendation.isFromSearch) Color(0xFF06B6D4) else SpotifyGreen

    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) StormBlackElevated
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                1.dp,
                if (isSelected) accentColor else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                MusicaImage(
                    model = recommendation.song.artworkUrl,
                    contentDescription = recommendation.song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    titlePlaceholder = recommendation.song.title
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = recommendation.song.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5F5F7),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = recommendation.song.artist,
                fontSize = 10.5.sp,
                color = Color(0xFFA1A1AA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (recommendation.isFromSearch) "🔍 Search" else "🎧 Listened",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
}

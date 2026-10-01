package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import com.example.ui.components.MusicaImage
import com.example.ui.components.SearchableTopBar
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect
import com.example.util.VibesHaptics

/**
 * Cleaned and high-performance Search Screen for Vibes.
 * Features:
 * - Fully functioning Google Voice Search button
 * - Shows up to 5 previous searches with cross 'X' deletion buttons when search bar is clicked
 * - Real-time songs ranked in strictly descending order of views/streams on Spotify
 * - Cleaned-up, clutter-free layout with smooth Spotify-grade ergonomics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    searchResults: List<Song>,
    searchHistory: List<String> = emptyList(),
    onRemoveSearchHistoryItem: (String) -> Unit = {},
    onClearSearchHistory: () -> Unit = {},
    catalogSongs: List<Song> = emptyList(),
    matchedArtist: Artist? = null,
    matchedArtists: List<Artist> = emptyList(),
    matchedAlbum: Album? = null,
    matchedAlbums: List<Album> = emptyList(),
    isSearching: Boolean,
    currentPlayingId: Long?,
    isPlaying: Boolean,
    favoriteSongs: List<Song>,
    followedArtists: List<Artist> = emptyList(),
    onQueryChanged: (String) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleFollowArtist: ((Artist) -> Unit)? = null,
    onArtistClick: ((String) -> Unit)? = null,
    onOpenAlbum: ((Album) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    // Track search bar interaction state
    var isSearchFocused by remember { mutableStateOf(false) }

    // Take exactly up to 5 previous searches
    val recentFiveSearches = remember(searchHistory) {
        searchHistory.filter { it.isNotBlank() }.distinct().take(5)
    }

    // List searched songs in strictly descending order of views on Spotify
    val songsSortedBySpotifyViews = remember(searchResults) {
        searchResults.sortedByDescending { it.spotifyStreams }
    }

    val effectiveArtists = remember(matchedArtists, matchedArtist) {
        if (matchedArtists.isNotEmpty()) matchedArtists
        else if (matchedArtist != null) listOf(matchedArtist)
        else emptyList()
    }

    val effectiveAlbums = remember(matchedAlbums, matchedAlbum) {
        val list = mutableListOf<Album>()
        if (matchedAlbum != null) {
            list.add(matchedAlbum)
        }
        for (a in matchedAlbums) {
            if (list.none { it.title.equals(a.title, ignoreCase = true) && it.artist.equals(a.artist, ignoreCase = true) }) {
                list.add(a)
            }
        }
        list
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Clean Header: "Search" Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Search",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WhiteSmoke,
                        letterSpacing = (-0.6).sp
                    )
                    Text(
                        text = "Find songs, artists & global Spotify hits",
                        fontSize = 12.5.sp,
                        color = WhiteSmokeMuted,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Spotify Powered Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpotifyGreen.copy(alpha = 0.15f))
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Spotify Views",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Spotify Views",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upgraded Searchable TopBar with Google Voice Search
            SearchableTopBar(
                query = query,
                onQueryChanged = { newQuery ->
                    onQueryChanged(newQuery)
                },
                searchResults = songsSortedBySpotifyViews,
                isSearching = isSearching,
                onPlaySong = { song -> onPlaySong(song, songsSortedBySpotifyViews) },
                onOpenSongDetails = onOpenSongDetails,
                onFocusChanged = { focused ->
                    isSearchFocused = focused
                },
                onClick = {
                    isSearchFocused = true
                },
                placeholder = "Search songs, artists, albums..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Content Area
            when {
                // 1. Loading State
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = SpotifyGreen,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Searching & ranking by Spotify views...",
                                fontSize = 13.5.sp,
                                color = WhiteSmokeMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 2. Active Query Results (Sorted by Spotify Views Descending)
                query.isNotBlank() -> {
                    if (songsSortedBySpotifyViews.isEmpty() && effectiveArtists.isEmpty() && effectiveAlbums.isEmpty()) {
                        // Empty Results
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(StormBlackElevated)
                                        .border(1.dp, StormSlateBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = WhiteSmokeMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No results found for \"$query\"",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteSmoke,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try searching by song title, artist name, or genre",
                                    fontSize = 13.sp,
                                    color = WhiteSmokeMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Clean Results List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Matching Artists (Compact row)
                            if (effectiveArtists.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Matching Artists",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteSmokeSoft
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(effectiveArtists) { artistItem ->
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(StormBlackElevated)
                                                    .border(1.dp, StormSlateBorder, RoundedCornerShape(20.dp))
                                                    .clickable {
                                                        VibesHaptics.strongClick(context, view)
                                                        onArtistClick?.invoke(artistItem.name)
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!artistItem.imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = artistItem.imageUrl,
                                                        contentDescription = artistItem.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(SpotifyGreen.copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = SpotifyGreen,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = artistItem.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = WhiteSmoke
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Matching Albums Section (Showcasing Top 5 Songs & Full Tracklist Access)
                            if (effectiveAlbums.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = Color(0xFFA855F7),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Albums",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFA855F7).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Full Tracklist Available",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC084FC)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Primary Showcase Album with Top 5 Songs
                                    val primaryAlbum = effectiveAlbums.first()
                                    SearchAlbumShowcaseCard(
                                        album = primaryAlbum,
                                        currentPlayingId = currentPlayingId,
                                        isPlaying = isPlaying,
                                        onPlaySong = onPlaySong,
                                        onOpenAlbum = { album ->
                                            VibesHaptics.strongClick(context, view)
                                            onOpenAlbum?.invoke(album)
                                        }
                                    )

                                    // Secondary Matching Albums
                                    if (effectiveAlbums.size > 1) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "More Matching Albums",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WhiteSmokeMuted
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(effectiveAlbums.drop(1)) { album ->
                                                CompactAlbumSearchCard(
                                                    album = album,
                                                    onOpenAlbum = {
                                                        VibesHaptics.strongClick(context, view)
                                                        onOpenAlbum?.invoke(album)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                }
                            }

                            // Songs Section Header
                            if (songsSortedBySpotifyViews.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Top Songs by Spotify Views",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SpotifyGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Descending",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SpotifyGreen
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${songsSortedBySpotifyViews.size} tracks",
                                            fontSize = 12.sp,
                                            color = WhiteSmokeMuted
                                        )
                                    }
                                }

                                // Song Items strictly in descending order of Spotify views
                                itemsIndexed(songsSortedBySpotifyViews, key = { _, s -> s.id }) { index, song ->
                                    val isCurrent = currentPlayingId == song.id
                                    val isFav = favoriteSongs.any { it.id == song.id }

                                    CleanSearchSongRow(
                                        song = song,
                                        rankIndex = index + 1,
                                        isCurrent = isCurrent,
                                        isPlaying = isPlaying && isCurrent,
                                        isFav = isFav,
                                        onPlay = {
                                            VibesHaptics.playPause(context, view)
                                            onPlaySong(song, songsSortedBySpotifyViews)
                                        },
                                        onOpenDetails = {
                                            VibesHaptics.mediumClick(context, view)
                                            onOpenSongDetails(song)
                                        },
                                        onToggleFavorite = {
                                            VibesHaptics.strongClick(context, view)
                                            onToggleFavorite(song)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Empty Search / Search Bar Clicked: Show 5 Previous Searches with Cross Button
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 5 PREVIOUS SEARCHES WITH CROSS BUTTON (Requested Feature)
                        if (recentFiveSearches.isNotEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(StormBlackElevated)
                                        .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Recent Searches",
                                                tint = SpotifyGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Previous Searches",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                VibesHaptics.mediumClick(context, view)
                                                onClearSearchHistory()
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = "Clear all",
                                                fontSize = 11.5.sp,
                                                color = WhiteSmokeMuted,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Render up to 5 previous searches, each with its cross 'X' button
                                    recentFiveSearches.forEach { searchItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    VibesHaptics.strongClick(context, view)
                                                    onQueryChanged(searchItem)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = WhiteSmokeMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = searchItem,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = WhiteSmoke,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Cross 'X' Button to remove this previous search
                                            IconButton(
                                                onClick = {
                                                    VibesHaptics.strongClick(context, view)
                                                    onRemoveSearchHistoryItem(searchItem)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove search",
                                                    tint = WhiteSmokeMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Browse Top Trending Genres (Clean & Modern)
                        item {
                            Text(
                                text = "Explore Genres",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmoke
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val genreList = listOf(
                                "Pop" to Color(0xFFE91E63),
                                "Hip-Hop" to Color(0xFFF59E0B),
                                "R&B" to Color(0xFF8B5CF6),
                                "Rock" to Color(0xFFEF4444),
                                "Bollywood" to Color(0xFFEC4899),
                                "EDM" to Color(0xFF06B6D4),
                                "Latin" to Color(0xFF10B981),
                                "Indie" to Color(0xFF3B82F6)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(genreList) { (genreName, genreColor) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(genreColor.copy(alpha = 0.2f))
                                            .border(1.dp, genreColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                            .clickable {
                                                VibesHaptics.strongClick(context, view)
                                                onQueryChanged(genreName)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = genreName,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = WhiteSmoke
                                        )
                                    }
                                }
                            }
                        }

                        // Popular Artists Quick Discovery
                        item {
                            Text(
                                text = "Top Global Artists",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmoke
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val topArtists = listOf(
                                "Taylor Swift", "The Weeknd", "Billie Eilish",
                                "Bruno Mars", "Coldplay", "Drake", "Sabrina Carpenter", "Ed Sheeran"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(topArtists) { artistName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(StormBlackElevated)
                                            .border(1.dp, StormSlateBorder, RoundedCornerShape(20.dp))
                                            .clickable {
                                                VibesHaptics.strongClick(context, view)
                                                onQueryChanged(artistName)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SpotifyGreen,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = artistName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = WhiteSmoke
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
    }
}

/**
 * Ultra-clean search song row with prominent Spotify Views badge,
 * ranking index (#1, #2, etc.), tactile controls, and Spotify Green accents.
 */
@Composable
private fun CleanSearchSongRow(
    song: Song,
    rankIndex: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFav: Boolean,
    onPlay: () -> Unit,
    onOpenDetails: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val rankBadgeColor = when (rankIndex) {
        1 -> Color(0xFFFFD700) // Gold #1
        2 -> Color(0xFFC0C0C0) // Silver #2
        3 -> Color(0xFFCD7F32) // Bronze #3
        else -> WhiteSmokeMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrent) SpotifyGreen.copy(alpha = 0.12f) else StormBlackElevated.copy(alpha = 0.7f))
            .border(
                1.dp,
                if (isCurrent) SpotifyGreen.copy(alpha = 0.5f) else StormSlateBorder.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onOpenDetails() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "#$rankIndex",
                    fontSize = 13.sp,
                    fontWeight = if (rankIndex <= 3) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = rankBadgeColor
                )
            }

            // Song Artwork
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, StormSlateBorder, RoundedCornerShape(10.dp))
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                MusicaImage(
                    model = song.artworkUrl,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    titlePlaceholder = song.title
                )

                // Playing overlay
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Artist, & Spotify Views Badge
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) SpotifyGreen else WhiteSmoke,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${song.artist} • ${song.album}",
                    fontSize = 12.sp,
                    color = WhiteSmokeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Prominent Spotify Views Badge (sorted descending)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SpotifyGreen.copy(alpha = 0.16f))
                        .border(0.8.dp, SpotifyGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Streams",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${song.formattedSpotifyStreams} Spotify views",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                }
            }

            // Action Buttons: Favorite + Play
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Heart Favorite Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color(0xFFF472B6) else WhiteSmokeMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent && isPlaying) SpotifyGreen else StormBlackCard)
                        .border(1.dp, SpotifyGreen.copy(alpha = 0.6f), CircleShape)
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isCurrent && isPlaying) Color.Black else SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Showcase card for an album matching search query.
 * Displays:
 * - Album artwork, title, artist, year, track count badge
 * - Header click or button to open full album bottom sheet (displaying all songs)
 * - Top 5 songs in the album with rank badges, Spotify stream counts, and play/pause controls
 * - "View All X Songs" interactive footer button
 */
@Composable
fun SearchAlbumShowcaseCard(
    album: Album,
    currentPlayingId: Long?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val top5Songs = remember(album) {
        album.topFeaturedSongs.take(5).ifEmpty { album.tracks.take(5) }
    }
    val effectiveTrackCount = remember(album) {
        album.trackCount.coerceAtLeast(album.tracks.size).coerceAtLeast(top5Songs.size)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
            .border(1.dp, StormSlateBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Album Hero Header: Cover + Info + Open Album Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        VibesHaptics.strongClick(context, view)
                        onOpenAlbum(album)
                    }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Cover
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, StormSlateBorder, RoundedCornerShape(12.dp))
                ) {
                    MusicaImage(
                        model = album.artworkUrl,
                        contentDescription = album.title,
                        titlePlaceholder = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Artist, & Meta Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ALBUM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC084FC)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${album.releaseYear} • $effectiveTrackCount TRACKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WhiteSmokeMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = album.title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = album.artist,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpotifyGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Quick Play/Open Pill
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            VibesHaptics.strongClick(context, view)
                            onOpenAlbum(album)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = "Open Album",
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subheader: Top 5 Songs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top 5 Songs",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteSmokeSoft
                )
                Text(
                    text = "Spotify Popularity",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteSmokeMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Top 5 Songs List
            if (top5Songs.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    top5Songs.forEachIndexed { index, song ->
                        val isCurrent = currentPlayingId == song.id
                        val rank = index + 1
                        val rankColor = when (rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> WhiteSmokeMuted
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) SpotifyGreen.copy(alpha = 0.12f) else StormBlackCard.copy(alpha = 0.6f))
                                .border(
                                    0.8.dp,
                                    if (isCurrent) SpotifyGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.04f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    VibesHaptics.mediumClick(context, view)
                                    val playlist = album.tracks.ifEmpty { top5Songs }
                                    onPlaySong(song, playlist)
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number
                            Text(
                                text = "#$rank",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = rankColor,
                                modifier = Modifier.width(24.dp)
                            )

                            // Title & Spotify Stream Count
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) SpotifyGreen else WhiteSmoke,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Equalizer,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${song.formattedSpotifyStreams} views",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SpotifyGreen
                                    )
                                }
                            }

                            // Play / Pause Icon
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent && isPlaying) SpotifyGreen else Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Track",
                                    tint = if (isCurrent && isPlaying) Color.Black else SpotifyGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap below to view full tracklist",
                        fontSize = 12.sp,
                        color = WhiteSmokeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Button: View All Songs in Album
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        VibesHaptics.strongClick(context, view)
                        onOpenAlbum(album)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "View All $effectiveTrackCount Songs in Album",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE9D5FF)
                    )
                }
            }
        }
    }
}

/**
 * Compact card for secondary albums matching search.
 */
@Composable
fun CompactAlbumSearchCard(
    album: Album,
    onOpenAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackCount = album.trackCount.coerceAtLeast(album.tracks.size).coerceAtLeast(album.topFeaturedSongs.size)

    Column(
        modifier = modifier
            .width(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StormBlackElevated)
            .border(1.dp, StormSlateBorder, RoundedCornerShape(14.dp))
            .clickable { onOpenAlbum(album) }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, StormSlateBorder, RoundedCornerShape(10.dp))
        ) {
            MusicaImage(
                model = album.artworkUrl,
                contentDescription = album.title,
                titlePlaceholder = album.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = album.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = WhiteSmoke,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = album.artist,
            fontSize = 11.sp,
            color = WhiteSmokeMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$trackCount Tracks",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFC084FC)
        )
    }
}


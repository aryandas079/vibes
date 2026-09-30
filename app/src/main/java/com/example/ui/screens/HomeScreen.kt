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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.MusicaImage
import com.example.model.Album
import com.example.model.Artist
import com.example.model.DiscoveryRecommendation
import com.example.model.HistoryItem
import com.example.model.Song
import com.example.model.SyncStatus
import com.example.model.UserSession
import com.example.ui.components.GeminiDiscoverySection
import com.example.ui.components.HomeAuthHeader
import com.example.ui.theme.GenreHipHopBg
import com.example.ui.theme.*

data class GenreItem(
    val name: String,
    val vectorColor: Color,
    val imageUrl: String
)

@Composable
fun HomeScreen(
    featuredSong: Song?,
    trendingSongs: List<Song>,
    categorySongs: List<Song>,
    recommendedSongs: List<Song>,
    selectedCategory: String,
    topArtists: List<Artist>,
    favoriteSongs: List<Song>,
    isLoading: Boolean,
    isPlaying: Boolean,
    onSelectCategory: (String) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onArtistClick: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenGenre: (String) -> Unit = {},
    historyItems: List<HistoryItem> = emptyList(),
    currentPlayingId: Long? = null,
    discoveryRecommendations: List<DiscoveryRecommendation> = emptyList(),
    isDiscoveryLoading: Boolean = false,
    onRefreshDiscovery: () -> Unit = {},
    onOpenLyrics: ((Song) -> Unit)? = null,
    themeMode: com.example.model.AppThemeMode = com.example.model.AppThemeMode.DARK,
    onToggleTheme: (() -> Unit)? = null,
    userSession: UserSession? = null,
    syncStatus: SyncStatus = SyncStatus.IDLE,
    onOpenAuth: () -> Unit = {},
    deviceSongs: List<Song> = emptyList(),
    isOfflineMode: Boolean = false,
    isScanningDeviceFiles: Boolean = false,
    onSyncDeviceFiles: () -> Unit = {},
    onToggleOfflineMode: () -> Unit = {},
    onOpenMoodPlaylistGenerator: () -> Unit = {},
    featuredAlbums: List<Album> = emptyList(),
    onOpenAlbum: (Album) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    searchResults: List<Song> = emptyList(),
    isSearching: Boolean = false,
    dailyMixSongs: List<Song> = emptyList(),
    dailyMixLastUpdated: Long = 0L,
    onRefreshDailyMix: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    val genres = listOf(
        GenreItem("Pop", GenrePopBg, "https://cdn-images.dzcdn.net/images/misc/f9e070848998df8870ba65cd0d22b2b3/500x500-000000-80-0-0.jpg"),
        GenreItem("Hip-Hop", GenreHipHopBg, "https://cdn-images.dzcdn.net/images/misc/5b9750b2964e5264b387402a11b8dbbe/500x500-000000-80-0-0.jpg"),
        GenreItem("Rock", GenreRockBg, "https://cdn-images.dzcdn.net/images/misc/f14f9fde9feb38ca6d61960f00681860/500x500-000000-80-0-0.jpg"),
        GenreItem("Latin", GenreLatinBg, "https://cdn-images.dzcdn.net/images/misc/069c9888538799748960781f098b5f4b/500x500-000000-80-0-0.jpg"),
        GenreItem("K-Pop", GenreKPopBg, "https://cdn-images.dzcdn.net/images/misc/dd6d2756465b22488dff5d8663e86688/500x500-000000-80-0-0.jpg"),
        GenreItem("R&B", GenreRnBBg, "https://cdn-images.dzcdn.net/images/misc/3d5e8aab99b95bfa7ac7e9e466e7781e/500x500-000000-80-0-0.jpg")
    )

    val sortedGenres = remember(selectedCategory, historyItems) {
        val historyGenres = historyItems.map { it.song.genre }.distinct()
        genres.sortedByDescending { genre ->
            when {
                genre.name.equals(selectedCategory, ignoreCase = true) -> 100
                genre.name in historyGenres -> 50
                else -> 0
            }
        }
    }

    // Dynamic recommendations based on history & searches
    val displayRecs = recommendedSongs.ifEmpty {
        if (categorySongs.isNotEmpty()) categorySongs else trendingSongs
    }

    val vibeMixSongs = remember(trendingSongs, recommendedSongs) {
        val base = if (trendingSongs.isNotEmpty()) trendingSongs else recommendedSongs
        base.shuffled().take(6)
    }
    val daySoundtrackSongs = remember(recommendedSongs, trendingSongs) {
        val base = if (recommendedSongs.isNotEmpty()) recommendedSongs else trendingSongs
        base.shuffled().take(6)
    }

    // Speed Dial: 4 most listened songs and 2 most listened albums strictly prioritized by play count in history
    val (speedDialSongs, speedDialAlbums) = remember(historyItems, favoriteSongs, trendingSongs, featuredSong, featuredAlbums) {
        val songPlayCounts = historyItems.groupingBy { it.song.id }.eachCount()
        val songsFromHistory = historyItems
            .map { it.song }
            .distinctBy { it.id }
            .sortedByDescending { songPlayCounts[it.id] ?: 0 }

        val fallbackSongs = (favoriteSongs + listOfNotNull(featuredSong) + trendingSongs).distinctBy { it.id }
        val topSongs = (songsFromHistory + fallbackSongs).distinctBy { it.id }.take(4)

        // 2 most listened albums strictly prioritized by play count in history
        val albumPlayCounts = historyItems
            .filter { it.song.album.isNotBlank() && !it.song.album.equals("Single", ignoreCase = true) }
            .groupingBy { it.song.album }
            .eachCount()

        val sortedAlbumNames = albumPlayCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val albumsFromHistory = sortedAlbumNames.mapNotNull { albumName ->
            featuredAlbums.firstOrNull { it.title.equals(albumName, ignoreCase = true) }
                ?: run {
                    val matchingSongs = historyItems.map { it.song }.filter { it.album.equals(albumName, ignoreCase = true) }.distinctBy { it.id }
                    val rep = matchingSongs.firstOrNull()
                    rep?.let {
                        Album(
                            id = it.id,
                            title = it.album,
                            artist = it.artist,
                            artworkUrl = it.artworkUrl,
                            genre = it.genre,
                            releaseYear = it.releaseYear,
                            trackCount = matchingSongs.size,
                            tracks = matchingSongs
                        )
                    }
                }
        }

        val fallbackAlbums = featuredAlbums
        val topAlbums = (albumsFromHistory + fallbackAlbums).distinctBy { it.title.lowercase() }.take(2)

        Pair(topSongs, topAlbums)
    }

    val hasHistory = historyItems.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Top Bar with Authentication & Dynamic Greeting
            item {
                HomeAuthHeader(
                    userSession = userSession,
                    syncStatus = syncStatus,
                    onOpenAuth = onOpenAuth,
                    onNavigateToSearch = onNavigateToSearch,
                    isOfflineMode = isOfflineMode,
                    onToggleOfflineMode = onToggleOfflineMode,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    searchResults = searchResults,
                    isSearching = isSearching,
                    onPlaySong = onPlaySong,
                    onOpenSongDetails = onOpenSongDetails
                )
            }

            // Device Local Storage Offline Sync Banner Card
            if (isOfflineMode) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
                            .border(
                                width = 1.dp,
                                color = if (isOfflineMode) SpotifyGreen.copy(alpha = 0.5f) else colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isOfflineMode) SpotifyGreen.copy(alpha = 0.2f) else colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.PhoneAndroid,
                                            contentDescription = "Device Storage",
                                            tint = if (isOfflineMode) SpotifyGreen else colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isOfflineMode) "Offline Device Mode" else "Sync Device Music",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isOfflineMode) SpotifyGreen else colorScheme.secondaryContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isOfflineMode) "OFFLINE ACTIVE" else "LOCAL SD",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isOfflineMode) StormBlackBg else colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${deviceSongs.size} tracks scanned from device storage",
                                            fontSize = 11.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Sync / Scan button
                                IconButton(
                                    onClick = onSyncDeviceFiles,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.surfaceVariant)
                                ) {
                                    if (isScanningDeviceFiles) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = SpotifyGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Sync Local Files",
                                            tint = colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Play Offline Songs Row if offline mode or synced
                            if (deviceSongs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SpotifyGreen)
                                            .clickable {
                                                deviceSongs.firstOrNull()?.let { onPlaySong(it, deviceSongs) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play Offline",
                                                tint = StormBlackBg,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Play All Offline Tracks",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StormBlackBg
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isOfflineMode) SpotifyGreen.copy(alpha = 0.25f)
                                                else colorScheme.surfaceVariant
                                            )
                                            .clickable { onToggleOfflineMode() }
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isOfflineMode) "Online Mode" else "Toggle Offline",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isOfflineMode) SpotifyGreen else colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Device Files Offline Audio Section
            if (isOfflineMode && deviceSongs.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SdCard,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Device Offline Storage",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${deviceSongs.size} local tracks",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(deviceSongs) { song ->
                                val isCurrent = song.id == currentPlayingId

                                Column(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                                        .clickable {
                                            onPlaySong(song, deviceSongs)
                                            onOpenSongDetails(song)
                                        }
                                        .padding(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        Box(
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .align(Alignment.TopStart)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StormBlackBg.copy(alpha = 0.85f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "OFFLINE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = SpotifyGreen
                                            )
                                        }

                                        if (isCurrent && isPlaying) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.4f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = "Playing",
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = song.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = song.artist,
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Section 1: "Speed Dial" 2-Column Grid (4 most listened songs + 2 most listened albums based on history)
            if (speedDialSongs.isNotEmpty() || speedDialAlbums.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Speed Dial",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (hasHistory) SpotifyGreen.copy(alpha = 0.2f) else colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (hasHistory) "Top Listened" else "Quick Play",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasHistory) SpotifyGreen else colorScheme.primary
                                    )
                                }
                            }

                            if (hasHistory) {
                                Text(
                                    text = "Top Tracks & Albums",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2-Column Grid for Speed Dial items: 4 Most Listened Songs + 2 Most Listened Albums
                        val songPairs = speedDialSongs.take(4).chunked(2)
                        val albumPairs = speedDialAlbums.take(2).chunked(2)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 4 Most Listened Songs
                            songPairs.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { song ->
                                        val isCurrent = song.id == currentPlayingId
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(58.dp)
                                                .liquidGlassEffect(
                                                    shape = RoundedCornerShape(12.dp),
                                                    elevation = if (isCurrent) 6.dp else 2.dp
                                                )
                                                .clickable {
                                                    onPlaySong(song, speedDialSongs)
                                                    onOpenSongDetails(song)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            MusicaImage(
                                                model = song.artworkUrl,
                                                contentDescription = song.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(58.dp)
                                                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                                                titlePlaceholder = song.title
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 6.dp),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = song.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = song.artist,
                                                    fontSize = 11.sp,
                                                    color = colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (isCurrent && isPlaying) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(SpotifyGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GraphicEq,
                                                        contentDescription = "Playing",
                                                        tint = StormBlackBg,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Balance row if odd count
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // 2 Most Listened Albums
                            albumPairs.forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { album ->
                                        val isAlbumPlaying = currentPlayingId != null && album.tracks.any { it.id == currentPlayingId } && isPlaying
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(58.dp)
                                                .liquidGlassEffect(
                                                    shape = RoundedCornerShape(12.dp),
                                                    elevation = if (isAlbumPlaying) 6.dp else 2.dp
                                                )
                                                .clickable {
                                                    if (album.tracks.isNotEmpty()) {
                                                        onPlaySong(album.tracks.first(), album.tracks)
                                                    }
                                                    onOpenAlbum(album)
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            MusicaImage(
                                                model = album.artworkUrl,
                                                contentDescription = album.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(58.dp)
                                                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                                                titlePlaceholder = album.title
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 6.dp),
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = album.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isAlbumPlaying) SpotifyGreen else colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Album • ${album.artist}",
                                                    fontSize = 11.sp,
                                                    color = colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (isAlbumPlaying) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(SpotifyGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GraphicEq,
                                                        contentDescription = "Playing",
                                                        tint = StormBlackBg,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Balance row if odd count
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Section: Recent Plays (Stores the last 10 songs the user interacted with)
            if (historyItems.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SpotifyGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Recent Plays",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Recent Plays",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    letterSpacing = (-0.3).sp
                                )
                            }
                            
                            Text(
                                text = "Revisit Tracks",
                                fontSize = 11.sp,
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(historyItems.take(10), key = { it.historyId }) { historyItem ->
                                val song = historyItem.song
                                val isCurrent = song.id == currentPlayingId

                                Column(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clickable {
                                            onPlaySong(song, historyItems.map { it.song })
                                            onOpenSongDetails(song)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        if (isCurrent && isPlaying) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GraphicEq,
                                                    contentDescription = "Playing",
                                                    tint = SpotifyGreen,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = song.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = song.artist,
                                        fontSize = 10.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Section: Gemini AI Song Discovery
            item {
                GeminiDiscoverySection(
                    recommendations = discoveryRecommendations,
                    isLoading = isDiscoveryLoading,
                    onPlaySong = { song ->
                        val playlist = discoveryRecommendations.map { it.song }
                        onPlaySong(song, playlist)
                    },
                    onRefreshDiscovery = onRefreshDiscovery,
                    onOpenLyrics = { song ->
                        onOpenLyrics?.invoke(song)
                    },
                    onToggleFavorite = onToggleFavorite,
                    favoriteSongIds = favoriteSongs.map { it.id }.toSet(),
                    currentPlayingId = currentPlayingId,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Featured Albums
            if (featuredAlbums.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                                                listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = "Featured Albums",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Featured Albums",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                            letterSpacing = (-0.3).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEC4899).copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${featuredAlbums.size} ALBUMS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFF472B6)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Complete studio records & full tracklists",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(featuredAlbums, key = { it.id }) { album ->
                                val isAlbumPlaying = currentPlayingId != null && album.tracks.any { it.id == currentPlayingId } && isPlaying
                                AlbumCardItem(
                                    album = album,
                                    isPlaying = isAlbumPlaying,
                                    onClick = { onOpenAlbum(album) },
                                    onPlayAlbum = {
                                        if (album.tracks.isNotEmpty()) {
                                            onPlaySong(album.tracks.first(), album.tracks)
                                            onOpenSongDetails(album.tracks.first())
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Section: Mood & Activity Sequencer Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                        .border(1.dp, StormSlateBorder, RoundedCornerShape(18.dp))
                        .clickable { onOpenMoodPlaylistGenerator() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("mood_sequencer_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mood & Activity Mix",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sequence transitions, tempo & vibe to match your flow",
                                fontSize = 11.5.sp,
                                color = colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SpotifyGreen.copy(alpha = 0.15f))
                                .border(1.dp, SpotifyGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Create",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyGreen
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section 2: "Genres" with Geometric Vector Art & Live Billboard Top Charts
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Genres & Top Charts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any genre for real-time Billboard & World Top 50",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(sortedGenres) { genre ->
                            GenreVectorCard(
                                genre = genre,
                                isSelected = selectedCategory.equals(genre.name, ignoreCase = true),
                                onClick = {
                                    onSelectCategory(genre.name)
                                    onOpenGenre(genre.name)
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 3: "Artists" Circular Avatars (Dynamic Based on History & Taste)
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Artists For You",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = if (hasHistory) "Recommended from your listening history" else "Trending Billboard chart artists",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        items(topArtists) { artist ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable { onArtistClick(artist.name) }
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, WhiteSmoke.copy(alpha = 0.4f), CircleShape)
                                            .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                                    ) {
                                        MusicaImage(
                                            model = artist.imageUrl,
                                            contentDescription = artist.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = artist.name
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(WhiteSmoke)
                                            .border(1.5.dp, StormBlackBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Verified",
                                            tint = StormBlackBg,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = artist.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section: Daily Mix (10 tracks generated from Recent Plays & Trends, updated once every 24h)
            item {
                if (dailyMixSongs.isNotEmpty()) {
                    val remainingHours = remember(dailyMixLastUpdated) {
                        if (dailyMixLastUpdated == 0L) 24
                        else {
                            val elapsedMs = System.currentTimeMillis() - dailyMixLastUpdated
                            val remainingMs = (24 * 60 * 60 * 1000L) - elapsedMs
                            val remainingHours = (remainingMs / (1000 * 60 * 60)).coerceIn(0, 24)
                            if (remainingHours <= 0) 24 else remainingHours.toInt()
                        }
                    }

                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Daily Mix",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SpotifyGreen.copy(alpha = 0.2f))
                                            .border(1.dp, SpotifyGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "24H REFRESH",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SpotifyGreen
                                        )
                                    }
                                }
                                Text(
                                    text = "10 tracks based on recent plays & trends • Refreshes in ${remainingHours}h",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onRefreshDailyMix()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Daily Mix",
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Mix 10-Track Horizontal Carousel
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(dailyMixSongs) { index, song ->
                                Column(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable {
                                            onPlaySong(song, dailyMixSongs)
                                            onOpenSongDetails(song)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(150.dp)
                                            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 6.dp)
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        // Track Index Badge (#1 to #10)
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StormBlackBg.copy(alpha = 0.75f))
                                                .border(0.5.dp, WhiteSmoke.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }

                                        // Favorite button
                                        val isFav = favoriteSongs.any { it.id == song.id }
                                        IconButton(
                                            onClick = { onToggleFavorite(song) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(StormBlackBg.copy(alpha = 0.4f))
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) WhiteSmoke else WhiteSmokeDim,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Play Overlay Badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(SpotifyGreen)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = song.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Section 4: "Recommendations" (Dynamic based on history & searches)
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recommendations",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = if (recommendedSongs.isNotEmpty()) "Based on your search & listening history" else "Trending top global tracks",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isLoading && displayRecs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(displayRecs) { song ->
                                Column(
                                    modifier = Modifier
                                        .width(170.dp)
                                        .clickable {
                                            onPlaySong(song, displayRecs)
                                            onOpenSongDetails(song)
                                        }
                                ) {
                                    // Album Art Square Card with Liquid Glass
                                    Box(
                                        modifier = Modifier
                                            .size(170.dp)
                                            .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 6.dp)
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        // 30s preview badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StormBlackBg.copy(alpha = 0.65f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "30s preview",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }

                                        // Favorite button
                                        val isFav = favoriteSongs.any { it.id == song.id }
                                        IconButton(
                                            onClick = { onToggleFavorite(song) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(StormBlackBg.copy(alpha = 0.4f))
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) WhiteSmoke else WhiteSmokeDim,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Song Title
                                    Text(
                                        text = song.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Artist Name
                                    Text(
                                        text = song.artist,
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 5: "Vibe Mix for You"
            item {
                val songsToDisplay = vibeMixSongs
                if (songsToDisplay.isNotEmpty()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Vibe Mix for You",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "A dynamic curation of your top sounds",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(songsToDisplay) { song ->
                                Column(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable {
                                            onPlaySong(song, songsToDisplay)
                                            onOpenSongDetails(song)
                                        }
                                ) {
                                    // Album Art Square Card with Liquid Glass
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        // 30s preview badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StormBlackBg.copy(alpha = 0.65f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "30s preview",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }

                                        // Favorite button
                                        val isFav = favoriteSongs.any { it.id == song.id }
                                        IconButton(
                                            onClick = { onToggleFavorite(song) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StormBlackBg.copy(alpha = 0.4f))
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) WhiteSmoke else WhiteSmokeDim,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Song Title
                                    Text(
                                        text = song.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Artist Name
                                    Text(
                                        text = song.artist,
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Section 6: "Soundtrack of Your Day"
            item {
                val songsToDisplay = daySoundtrackSongs
                if (songsToDisplay.isNotEmpty()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Soundtrack of Your Day",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Fresh hits selected just for you",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(songsToDisplay) { song ->
                                Column(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable {
                                            onPlaySong(song, songsToDisplay)
                                            onOpenSongDetails(song)
                                        }
                                ) {
                                    // Album Art Square Card with Liquid Glass
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                                    ) {
                                        MusicaImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            titlePlaceholder = song.title
                                        )

                                        // 30s preview badge
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StormBlackBg.copy(alpha = 0.65f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "30s preview",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WhiteSmoke
                                            )
                                        }

                                        // Favorite button
                                        val isFav = favoriteSongs.any { it.id == song.id }
                                        IconButton(
                                            onClick = { onToggleFavorite(song) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(StormBlackBg.copy(alpha = 0.4f))
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFav) WhiteSmoke else WhiteSmokeDim,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Song Title
                                    Text(
                                        text = song.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Artist Name
                                    Text(
                                        text = song.artist,
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun GenreVectorCard(
    genre: GenreItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgCol = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
    val borderCol = if (isSelected) colorScheme.primary else colorScheme.outline
    val textColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
    val subTextColor = if (isSelected) colorScheme.onPrimary.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .width(140.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgCol)
            .border(
                width = 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = genre.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.onPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Selected",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
            } else {
                Text(
                    text = "Explore",
                    fontSize = 11.sp,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
fun AlbumCardItem(
    album: Album,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayAlbum: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .width(165.dp)
            .clickable(onClick = onClick)
            .testTag("album_card_${album.id}")
    ) {
        // High-resolution Album Artwork with Badges & Play Button
        Box(
            modifier = Modifier
                .size(165.dp)
                .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 6.dp)
                .border(1.dp, StormSlateBorder, RoundedCornerShape(20.dp))
        ) {
            MusicaImage(
                model = album.artworkUrl,
                contentDescription = album.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                titlePlaceholder = album.title
            )

            // Year Pill on Top-Left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StormBlackBg.copy(alpha = 0.82f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = album.releaseYear,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteSmoke
                )
            }

            // Track Count Pill on Bottom-Left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StormBlackBg.copy(alpha = 0.85f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${album.tracks.size} TRACKS",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = SpotifyGreen
                )
            }

            // Quick Play Button Floating on Bottom-Right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) SpotifyGreen else Color.Black.copy(alpha = 0.75f))
                    .clickable { onPlayAlbum() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Playing" else "Play Album",
                    tint = if (isPlaying) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Album Title
        Text(
            text = album.title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = (-0.2).sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Artist Name
        Text(
            text = album.artist,
            fontSize = 12.sp,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Genre
        Text(
            text = album.genre,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFC084FC)
        )
    }
}


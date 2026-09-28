package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Album
import com.example.model.Artist
import com.example.model.HistoryItem
import com.example.model.Song
import com.example.ui.theme.*

enum class LibraryTab(val title: String, val icon: ImageVector) {
    SAVED_TRACKS("Liked Songs", Icons.Default.Favorite),
    PLAYLISTS("Playlists", Icons.Default.Album),
    FOLLOWED_ARTISTS("Followed Artists", Icons.Default.Person),
    DEVICE_FILES("Device Files", Icons.Default.SdCard),
    ALBUMS("Albums", Icons.Default.Album),
    HISTORY("History", Icons.Default.History)
}

@Composable
fun FavoritesScreen(
    favoriteSongs: List<Song>,
    followedArtists: List<Artist> = emptyList(),
    historyItems: List<HistoryItem> = emptyList(),
    topArtists: List<Artist> = emptyList(),
    playlists: List<com.example.data.local.PlaylistEntity> = emptyList(),
    currentPlayingId: Long?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleFollowArtist: ((Artist) -> Unit)? = null,
    onArtistClick: ((String) -> Unit)? = null,
    onSelectPlaylist: (com.example.data.local.PlaylistEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    deviceSongs: List<Song> = emptyList(),
    isScanningDeviceFiles: Boolean = false,
    onSyncDeviceFiles: () -> Unit = {},
    onOpenMoodPlaylistGenerator: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(LibraryTab.SAVED_TRACKS) }
    var librarySearchQuery by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onSyncDeviceFiles()
    }

    // Filtered items based on in-library search
    val filteredFavorites = remember(favoriteSongs, librarySearchQuery) {
        if (librarySearchQuery.isBlank()) favoriteSongs
        else favoriteSongs.filter {
            it.title.contains(librarySearchQuery, ignoreCase = true) ||
            it.artist.contains(librarySearchQuery, ignoreCase = true) ||
            it.album.contains(librarySearchQuery, ignoreCase = true)
        }
    }

    val filteredFollowedArtists = remember(followedArtists, librarySearchQuery) {
        if (librarySearchQuery.isBlank()) followedArtists
        else followedArtists.filter {
            it.name.contains(librarySearchQuery, ignoreCase = true) ||
            it.genre.contains(librarySearchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header Title: "Library & Favorites" with Play All / Shuffle buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Library",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "${favoriteSongs.size} liked songs • ${followedArtists.size} followed artists",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                if (favoriteSongs.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle All Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                                .clickable {
                                    val shuffled = favoriteSongs.shuffled()
                                    shuffled.firstOrNull()?.let { onPlaySong(it, shuffled) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Play All Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SpotifyGreen)
                                .clickable {
                                    favoriteSongs.firstOrNull()?.let { onPlaySong(it, favoriteSongs) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play All",
                                tint = StormBlackBg,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Library Filter Bar / Sub-tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(LibraryTab.values()) { tab ->
                    val isSelected = selectedTab == tab
                    val countBadge = when (tab) {
                        LibraryTab.SAVED_TRACKS -> favoriteSongs.size
                        LibraryTab.PLAYLISTS -> playlists.size
                        LibraryTab.FOLLOWED_ARTISTS -> followedArtists.size
                        LibraryTab.DEVICE_FILES -> deviceSongs.size
                        LibraryTab.ALBUMS -> favoriteSongs.map { it.album }.distinct().size
                        LibraryTab.HISTORY -> historyItems.size
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) colorScheme.primary
                                else colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${tab.title} ($countBadge)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for filtering library items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 2.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (librarySearchQuery.isEmpty()) {
                        Text(
                            text = "Filter in your library...",
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = librarySearchQuery,
                        onValueChange = { librarySearchQuery = it },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(colorScheme.primary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (librarySearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { librarySearchQuery = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (selectedTab) {
                LibraryTab.PLAYLISTS -> {
                    val filteredPlaylists = remember(playlists, librarySearchQuery) {
                        if (librarySearchQuery.isBlank()) playlists
                        else playlists.filter { it.name.contains(librarySearchQuery, ignoreCase = true) || it.description.contains(librarySearchQuery, ignoreCase = true) }
                    }

                    if (filteredPlaylists.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF2E1065), StormBlackElevated)
                                        )
                                    )
                                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .clickable { onOpenMoodPlaylistGenerator() }
                                    .padding(14.dp)
                                    .testTag("library_create_gemini_playlist_empty")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = Color(0xFFC084FC),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Mood & Activity Mix",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF5F5F7)
                                        )
                                        Text(
                                            text = "Sequence transitions by tempo, mood & vibe",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            EmptyStateView(
                                icon = Icons.Default.Album,
                                title = if (librarySearchQuery.isBlank()) "No custom playlists yet" else "No matching playlists",
                                description = "Create playlists to group your favorite tracks together for any mood or occasion."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Mood & Activity Mix Prompt Item
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                                        .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
                                        .clickable { onOpenMoodPlaylistGenerator() }
                                        .padding(14.dp)
                                        .testTag("library_create_gemini_playlist_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = Color(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Mood & Activity Mix",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF5F5F7)
                                            )
                                            Text(
                                                text = "Sequence transitions by tempo, mood & vibe",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color(0xFFC084FC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            items(filteredPlaylists, key = { it.playlistId }) { playlist ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                                        .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
                                        .clickable { onSelectPlaylist(playlist) }
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SpotifyGreen.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = SpotifyGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playlist.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = playlist.description.ifBlank { "Custom Playlist" },
                                                fontSize = 12.sp,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. Liked Songs (Favorites saved to local Room DB)
                LibraryTab.SAVED_TRACKS -> {
                    if (filteredFavorites.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Favorite,
                            title = if (librarySearchQuery.isBlank()) "No liked songs yet" else "No matching songs in library",
                            description = if (librarySearchQuery.isBlank())
                                "Tap the heart icon on any track to save it here for fast offline access and quick playback."
                            else "Try searching for a different song or artist title."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(filteredFavorites, key = { _, s -> s.id }) { index, song ->
                                val isCurrent = currentPlayingId == song.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
                                        .clickable {
                                            onPlaySong(song, filteredFavorites)
                                            onOpenSongDetails(song)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) SpotifyGreen else colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(24.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${song.artist} • ${song.album}",
                                            fontSize = 11.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onToggleFavorite(song) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Remove Favorite",
                                            tint = WhiteSmoke,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            onPlaySong(song, filteredFavorites)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Followed Artists Section (saved in Room DB)
                LibraryTab.FOLLOWED_ARTISTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredFollowedArtists.isEmpty()) {
                            item {
                                EmptyStateView(
                                    icon = Icons.Default.Person,
                                    title = if (librarySearchQuery.isBlank()) "No followed artists yet" else "No matching artists found",
                                    description = "Follow artists to stay updated on their latest tracks and view their top hits directly here in your library."
                                )
                            }

                            // Suggestions to follow
                            if (topArtists.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Recommended Artists to Follow",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                items(topArtists) { artist ->
                                    val isFollowed = followedArtists.any { it.name.equals(artist.name, ignoreCase = true) }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                                            .clickable { onArtistClick?.invoke(artist.name) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                        ) {
                                            AsyncImage(
                                                model = artist.imageUrl,
                                                contentDescription = artist.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = artist.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${artist.genre} • ${artist.topHitsCount}",
                                                fontSize = 12.sp,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Button(
                                            onClick = { onToggleFollowArtist?.invoke(artist) },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowed) colorScheme.surfaceVariant else WhiteSmoke,
                                                contentColor = if (isFollowed) colorScheme.onSurfaceVariant else StormBlackBg
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isFollowed) "Following" else "Follow",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredFollowedArtists, key = { it.name }) { artist ->
                                val artistSongs = favoriteSongs.filter {
                                    it.artist.contains(artist.name, ignoreCase = true) || artist.name.contains(it.artist, ignoreCase = true)
                                }

                                LibraryFollowedArtistCard(
                                    artist = artist,
                                    songs = artistSongs,
                                    currentPlayingId = currentPlayingId,
                                    favoriteSongs = favoriteSongs,
                                    onPlaySong = onPlaySong,
                                    onOpenSongDetails = onOpenSongDetails,
                                    onToggleFavorite = onToggleFavorite,
                                    onUnfollow = { onToggleFollowArtist?.invoke(artist) },
                                    onOpenDiscography = { onArtistClick?.invoke(artist.name) }
                                )
                            }
                        }
                    }
                }

                // Device Audio Files (Offline Local SD Storage)
                LibraryTab.DEVICE_FILES -> {
                    val filteredDeviceSongs = remember(deviceSongs, librarySearchQuery) {
                        if (librarySearchQuery.isBlank()) deviceSongs
                        else deviceSongs.filter {
                            it.title.contains(librarySearchQuery, ignoreCase = true) ||
                            it.artist.contains(librarySearchQuery, ignoreCase = true) ||
                            it.album.contains(librarySearchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredDeviceSongs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.SdCard,
                            title = if (librarySearchQuery.isBlank()) "No local device files found" else "No matching local audio files",
                            description = if (librarySearchQuery.isBlank())
                                "Tap 'Sync Device Storage' below to scan your device for local MP3 and audio files."
                            else "Try searching for a different song or artist title."
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Header bar for local device files
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SpotifyGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "OFFLINE STORAGE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SpotifyGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${filteredDeviceSongs.size} tracks available",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            Manifest.permission.READ_MEDIA_AUDIO
                                        } else {
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        }
                                        permissionLauncher.launch(permissionToRequest)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.surfaceVariant,
                                        contentColor = colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Sync",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Sync Device Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(filteredDeviceSongs, key = { _, s -> s.id }) { index, song ->
                                    val isCurrent = currentPlayingId == song.id

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
                                            .clickable {
                                                onPlaySong(song, filteredDeviceSongs)
                                                onOpenSongDetails(song)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) SpotifyGreen else colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(24.dp)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = song.artworkUrl,
                                                contentDescription = song.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${song.artist} • ${song.album}",
                                                    fontSize = 12.sp,
                                                    color = colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SpotifyGreen.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "OFFLINE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SpotifyGreen
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { onPlaySong(song, filteredDeviceSongs) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isCurrent && isPlaying) SpotifyGreen else colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrent && isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = if (isCurrent && isPlaying) StormBlackBg else colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Albums Section
                LibraryTab.ALBUMS -> {
                    val groupedAlbums = remember(favoriteSongs) {
                        favoriteSongs.groupBy { it.album }.filter { it.key.isNotBlank() }
                    }

                    if (groupedAlbums.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Album,
                            title = "No albums in library",
                            description = "Songs you like will be organized into albums here automatically."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(groupedAlbums.entries.toList(), key = { it.key }) { (albumName, songs) ->
                                val firstSong = songs.first()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 3.dp)
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            AsyncImage(
                                                model = firstSong.artworkUrl,
                                                contentDescription = albumName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = albumName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${firstSong.artist} • ${songs.size} saved tracks",
                                                fontSize = 12.sp,
                                                color = colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = { onPlaySong(firstSong, songs) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SpotifyGreen)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play Album",
                                                tint = StormBlackBg,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Top 3 preview songs from album
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        songs.take(3).forEachIndexed { idx, s ->
                                            val isCurrent = currentPlayingId == s.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isCurrent) SpotifyGreen.copy(alpha = 0.12f)
                                                        else colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                    .clickable {
                                                        onPlaySong(s, songs)
                                                        onOpenSongDetails(s)
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCurrent) SpotifyGreen else colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.width(20.dp)
                                                )
                                                Text(
                                                    text = s.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { onPlaySong(s, songs) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Play",
                                                        tint = SpotifyGreen,
                                                        modifier = Modifier.size(16.dp)
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

                // 4. Listening History Tab
                LibraryTab.HISTORY -> {
                    if (historyItems.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            title = "No listening history yet",
                            description = "Songs you play will appear here so you can revisit them anytime."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(historyItems, key = { _, h -> h.historyId }) { index, item ->
                                val song = item.song
                                val isCurrent = currentPlayingId == song.id
                                val isFav = favoriteSongs.any { it.id == song.id }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
                                        .clickable {
                                            onPlaySong(song, historyItems.map { it.song })
                                            onOpenSongDetails(song)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = song.artworkUrl,
                                            contentDescription = song.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${song.artist} • ${song.album}",
                                            fontSize = 11.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onToggleFavorite(song) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFav) WhiteSmoke else colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onPlaySong(song, historyItems.map { it.song }) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(20.dp)
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

/**
 * Followed Artist Card in Library displaying verified avatar, follower stats,
 * and top 3 songs preview right inside the library!
 */
@Composable
private fun LibraryFollowedArtistCard(
    artist: Artist,
    songs: List<Song>,
    currentPlayingId: Long?,
    favoriteSongs: List<Song>,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onUnfollow: () -> Unit,
    onOpenDiscography: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, SpotifyGreen, CircleShape)
                    .clickable { onOpenDiscography() }
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = artist.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${artist.genre} • ${artist.topHitsCount}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onUnfollow,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Following",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick action: Discography / Top songs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDiscography() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Songs & Discography",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary
            )
            Text(
                text = "View All →",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .liquidGlassEffect(shape = CircleShape, elevation = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

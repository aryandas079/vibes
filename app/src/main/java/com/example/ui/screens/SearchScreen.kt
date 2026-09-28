package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.components.AmbientMusicRecognitionSheet
import com.example.ui.components.HummingSearchSheet
import com.example.ui.components.SearchableTopBar
import com.example.ui.components.VoiceSearchSheet
import androidx.compose.material.icons.filled.Hearing
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.model.Song
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    searchResults: List<Song>,
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
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedFilterTab by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Artists", "Albums", "Songs")

    var isVoiceSearchOpen by remember { mutableStateOf(false) }
    var isHummingSearchOpen by remember { mutableStateOf(false) }
    var isAmbientRecognitionOpen by remember { mutableStateOf(false) }
    val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hummingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ambientSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val effectiveArtists = remember(matchedArtists, matchedArtist) {
        if (matchedArtists.isNotEmpty()) {
            matchedArtists
        } else if (matchedArtist != null) {
            listOf(matchedArtist)
        } else {
            emptyList()
        }
    }

    val effectiveAlbums = remember(matchedAlbums, matchedAlbum, searchResults) {
        if (matchedAlbums.isNotEmpty()) {
            matchedAlbums
        } else if (matchedAlbum != null) {
            listOf(matchedAlbum)
        } else {
            // Group any song search results into albums if available
            val grouped = searchResults.filter { it.album.isNotBlank() }.groupBy { it.album }
            grouped.map { (albumTitle, songs) ->
                val first = songs.first()
                Album(
                    id = first.id,
                    title = albumTitle,
                    artist = first.artist,
                    artworkUrl = first.artworkUrl,
                    releaseYear = first.releaseYear,
                    genre = first.genre,
                    trackCount = songs.size,
                    tracks = songs,
                    topFeaturedSongs = songs.take(3)
                )
            }
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

            // Large Title: "Search"
            Text(
                text = "Search",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Searchable TopBar with External Music API metadata integration
            SearchableTopBar(
                query = query,
                onQueryChanged = onQueryChanged,
                searchResults = searchResults,
                isSearching = isSearching,
                onPlaySong = { song -> onPlaySong(song, searchResults) },
                onOpenSongDetails = onOpenSongDetails,
                onVoiceSearchClick = { isVoiceSearchOpen = true },
                placeholder = "Search songs, artists, metadata..."
            )



            // Quick Filter Tabs (when active search query exists)
            if (query.isNotBlank()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTabs) { tab ->
                        val isSelected = selectedFilterTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) colorScheme.primary
                                    else colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                                .clickable { selectedFilterTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Content / Results
            when {
                isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching music catalog...",
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                query.isBlank() -> {
                    // Empty Search suggestions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {


                        item {
                            Text(
                                text = "Popular Artists to Search",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val popularArtistChips = listOf(
                                "Taylor Swift", "The Weeknd", "Billie Eilish",
                                "Ed Sheeran", "Drake", "Coldplay", "Sabrina Carpenter"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(popularArtistChips) { artistName ->
                                    Box(
                                        modifier = Modifier
                                            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                                            .clickable { onQueryChanged(artistName) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SpotifyGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = artistName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Popular Albums to Explore",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val popularAlbumChips = listOf(
                                "Midnights", "Starboy", "Hit Me Hard and Soft",
                                "Divide", "1989 (Taylor's Version)", "Music of the Spheres"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(popularAlbumChips) { albumName ->
                                    Box(
                                        modifier = Modifier
                                            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                                            .clickable { onQueryChanged(albumName) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = albumName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Search for artists, albums, or tracks",
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                searchResults.isEmpty() && effectiveArtists.isEmpty() && effectiveAlbums.isEmpty() -> {
                    // No results
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No results found for \"$query\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching by a different artist, album, or song title",
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // Rich Results View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Matched Artist Cards (with small, high-density Top 3 tracks list directly clickable for immediate playback)
                        if ((selectedFilterTab == "All" || selectedFilterTab == "Artists") && effectiveArtists.isNotEmpty()) {
                            val artistsToShow = if (selectedFilterTab == "All") effectiveArtists.take(2) else effectiveArtists

                            if (selectedFilterTab == "Artists" || (selectedFilterTab == "All" && artistsToShow.size > 1)) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = SpotifyGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (artistsToShow.size == 1) "Featured Artist" else "Matching Artists",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.onBackground
                                            )
                                        }
                                        if (effectiveArtists.size > 2 && selectedFilterTab == "All") {
                                            Text(
                                                text = "${effectiveArtists.size} total",
                                                fontSize = 12.sp,
                                                color = SpotifyGreen,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.clickable { selectedFilterTab = "Artists" }
                                            )
                                        }
                                    }
                                }
                            }

                            itemsIndexed(artistsToShow, key = { index, art -> "${art.name}_$index" }) { _, artistItem ->
                                val isFollowed = followedArtists.any { it.name.equals(artistItem.name, ignoreCase = true) } || artistItem.isFollowed
                                ArtistRichCard(
                                    artist = artistItem,
                                    isFollowed = isFollowed,
                                    currentPlayingId = currentPlayingId,
                                    isPlaying = isPlaying,
                                    favoriteSongs = favoriteSongs,
                                    onPlaySong = onPlaySong,
                                    onOpenSongDetails = onOpenSongDetails,
                                    onToggleFavorite = onToggleFavorite,
                                    onToggleFollow = { onToggleFollowArtist?.invoke(artistItem) },
                                    onArtistClick = { onArtistClick?.invoke(artistItem.name) }
                                )
                            }
                        }

                        // 2. Rich Album Display Cards highlighting cover art and complete tracklist
                        if ((selectedFilterTab == "All" || selectedFilterTab == "Albums") && effectiveAlbums.isNotEmpty()) {
                            val albumsToShow = if (selectedFilterTab == "All") effectiveAlbums.take(2) else effectiveAlbums

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
                                            tint = SpotifyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (albumsToShow.size == 1) "Featured Album" else "Matching Albums",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onBackground
                                        )
                                    }
                                    if (effectiveAlbums.size > 2 && selectedFilterTab == "All") {
                                        Text(
                                            text = "${effectiveAlbums.size} total",
                                            fontSize = 12.sp,
                                            color = SpotifyGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.clickable { selectedFilterTab = "Albums" }
                                        )
                                    }
                                }
                            }

                            itemsIndexed(albumsToShow, key = { index, alb -> "${alb.title}_${alb.artist}_$index" }) { _, albumItem ->
                                AlbumDisplayCard(
                                    album = albumItem,
                                    currentPlayingId = currentPlayingId,
                                    isPlaying = isPlaying,
                                    favoriteSongs = favoriteSongs,
                                    onPlaySong = onPlaySong,
                                    onOpenSongDetails = onOpenSongDetails,
                                    onToggleFavorite = onToggleFavorite,
                                    onArtistClick = onArtistClick
                                )
                            }
                        }

                        // 3. Songs Section Header (when showing all results or songs tab)
                        if ((selectedFilterTab == "All" || selectedFilterTab == "Songs") && searchResults.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (effectiveArtists.isNotEmpty() || effectiveAlbums.isNotEmpty()) "All Track Results" else "Top Matches",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${searchResults.size} tracks",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            itemsIndexed(searchResults, key = { _, song -> song.id }) { index, song ->
                                val isFav = favoriteSongs.any { it.id == song.id }
                                val isCurrent = currentPlayingId == song.id

                                SearchSongRow(
                                    song = song,
                                    index = index + 1,
                                    isCurrent = isCurrent,
                                    isFav = isFav,
                                    onPlay = { onPlaySong(song, searchResults) },
                                    onOpenDetails = { onOpenSongDetails(song) },
                                    onToggleFavorite = { onToggleFavorite(song) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Voice Search Modal Bottom Sheet
    if (isVoiceSearchOpen) {
        VoiceSearchSheet(
            sheetState = voiceSheetState,
            onDismiss = { isVoiceSearchOpen = false },
            onQueryRecognized = { recognizedQuery ->
                isVoiceSearchOpen = false
                onQueryChanged(recognizedQuery)
            }
        )
    }

    // Search with Humming Modal Bottom Sheet
    if (isHummingSearchOpen) {
        HummingSearchSheet(
            sheetState = hummingSheetState,
            catalogSongs = catalogSongs.ifEmpty { searchResults },
            onDismiss = { isHummingSearchOpen = false },
            onPlaySong = { song ->
                isHummingSearchOpen = false
                onPlaySong(song, listOf(song))
            },
            onSearchSong = { songTitle ->
                isHummingSearchOpen = false
                onQueryChanged(songTitle)
            }
        )
    }

    // Ambient Music Recognition Modal Bottom Sheet
    if (isAmbientRecognitionOpen) {
        AmbientMusicRecognitionSheet(
            sheetState = ambientSheetState,
            catalogSongs = catalogSongs.ifEmpty { searchResults },
            onDismiss = { isAmbientRecognitionOpen = false },
            onPlaySong = { song ->
                isAmbientRecognitionOpen = false
                onPlaySong(song, listOf(song))
            },
            onOpenSongDetails = { song ->
                isAmbientRecognitionOpen = false
                onOpenSongDetails(song)
            }
        )
    }
}

/**
 * Rich Artist Card displaying verified avatar, follower badge, Follow/Unfollow toggle,
 * and a small, high-density list of their 'Top 3' tracks directly clickable for immediate playback,
 * plus the discography navigation link below.
 */
@Composable
fun ArtistRichCard(
    artist: Artist,
    isFollowed: Boolean,
    currentPlayingId: Long?,
    isPlaying: Boolean,
    favoriteSongs: List<Song>,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleFollow: () -> Unit,
    onArtistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val topThreeSongs = artist.topSongs.take(3)
    val remainingSongs = artist.topSongs.drop(3)
    val isArtistActive = currentPlayingId != null && artist.topSongs.any { it.id == currentPlayingId }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shape = RoundedCornerShape(22.dp),
                elevation = if (isArtistActive) 8.dp else 5.dp
            )
            .border(
                width = 1.dp,
                color = if (isArtistActive) WhiteSmoke.copy(alpha = 0.5f) else WhiteSmoke.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        // Artist Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(2.dp, SpotifyGreen.copy(alpha = 0.75f), CircleShape)
                    .clickable { onArtistClick() }
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Artist",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${artist.genre} • ${artist.topHitsCount}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Follow Button (Saves directly to Room DB)
            Button(
                onClick = onToggleFollow,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) colorScheme.surfaceVariant else WhiteSmoke,
                    contentColor = if (isFollowed) colorScheme.onSurfaceVariant else StormBlackBg
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("follow_artist_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Default.Check else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFollowed) "Following" else "Follow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top 3 Tracks Header with Quick "Play All" Pill Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    text = "Top 3 Tracks",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SpotifyGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Instant Play",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpotifyGreen
                    )
                }
            }

            if (topThreeSongs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SpotifyGreen.copy(alpha = 0.16f))
                        .clickable {
                            onPlaySong(topThreeSongs.first(), artist.topSongs)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All Top 3",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Play All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        if (topThreeSongs.isEmpty()) {
            Text(
                text = "Loading top tracks...",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            // High-density Top 3 List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                topThreeSongs.forEachIndexed { index, song ->
                    val isFav = favoriteSongs.any { it.id == song.id }
                    val isCurrent = currentPlayingId == song.id

                    ArtistTopTrackDenseRow(
                        rank = index + 1,
                        song = song,
                        isCurrent = isCurrent,
                        isPlaying = isPlaying,
                        isFav = isFav,
                        onPlayImmediate = { onPlaySong(song, artist.topSongs) },
                        onToggleFavorite = { onToggleFavorite(song) }
                    )
                }
            }
        }

        // Discography Link
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onArtistClick() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (remainingSongs.isNotEmpty()) "More by ${artist.name} (${remainingSongs.size} more tracks)" else "View Artist Profile & Discography",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.primary
            )
            Text(
                text = "Discography →",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary
            )
        }
    }
}

/**
 * Small, high-density track row specifically engineered for Artist search result cards.
 * Compact visual footprint with rank badges, album art, title, metadata, Room DB favorite toggle,
 * and immediate playback on direct click.
 */
@Composable
fun ArtistTopTrackDenseRow(
    rank: Int,
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFav: Boolean,
    onPlayImmediate: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val rankBadgeBg = when (rank) {
        1 -> WhiteSmoke.copy(alpha = 0.22f)
        2 -> WhiteSmoke.copy(alpha = 0.16f)
        3 -> WhiteSmoke.copy(alpha = 0.10f)
        else -> StormBlackElevated
    }

    val rankBadgeTextColor = when (rank) {
        1 -> WhiteSmoke
        2 -> WhiteSmokeSoft
        3 -> WhiteSmokeMuted
        else -> colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCurrent) SpotifyGreen.copy(alpha = 0.16f)
                else colorScheme.surfaceVariant.copy(alpha = 0.28f)
            )
            .border(
                width = if (isCurrent) 1.dp else 0.5.dp,
                color = if (isCurrent) WhiteSmoke.copy(alpha = 0.6f) else WhiteSmoke.copy(alpha = 0.05f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onPlayImmediate() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank pill or animated visualizer
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCurrent) SpotifyGreen.copy(alpha = 0.25f) else rankBadgeBg),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent && isPlaying) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Playing",
                    tint = SpotifyGreen,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Text(
                    text = "#$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) SpotifyGreen else rankBadgeTextColor
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // High-density Mini Artwork Thumbnail
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(0.5.dp, WhiteSmoke.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
        ) {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and High-density subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${song.album.ifBlank { "Popular Track" }} • 30s preview",
                fontSize = 10.5.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Room DB Favorite Heart Button
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFav) WhiteSmoke else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Immediate Play Button
        IconButton(
            onClick = onPlayImmediate,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                contentDescription = "Play Track Immediately",
                tint = SpotifyGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Enhanced Album Display Card for Search Results.
 * Highlights the album cover art (vinyl & sleeve aesthetic, atmospheric backdrop, verified artist metadata)
 * and interactive tracklist (Top 3 featured tracks with animated equalizer and expandable full tracklist),
 * ensuring visual consistency with the artist profile layout.
 */
@Composable
fun AlbumDisplayCard(
    album: Album,
    currentPlayingId: Long?,
    isPlaying: Boolean,
    favoriteSongs: List<Song>,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onArtistClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isTracklistExpanded by remember { mutableStateOf(false) }

    val topThreeTracks = album.topFeaturedSongs.ifEmpty { album.tracks.take(3) }
    val remainingTracks = album.tracks.drop(3)
    val isAlbumActive = currentPlayingId != null && album.tracks.any { it.id == currentPlayingId }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassEffect(
                shape = RoundedCornerShape(22.dp),
                elevation = if (isAlbumActive) 8.dp else 4.dp
            )
            .border(
                width = 1.dp,
                color = if (isAlbumActive) WhiteSmoke.copy(alpha = 0.5f) else WhiteSmoke.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        // 1. Cover Art & Album Details Hero Section (Consistent with Artist Profile Aesthetic)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Resolution Cover Art with Vinyl Sleeve Accent & Glowing Border
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, WhiteSmoke.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable {
                        album.tracks.firstOrNull()?.let { onPlaySong(it, album.tracks) }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = album.artworkUrl,
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    StormBlackBg.copy(alpha = 0.45f)
                                )
                            )
                        )
                )

                // Mini Vinyl Disc Indicator / Play Overlay
                if (isAlbumActive && isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StormBlackBg.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing Album",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata column
            Column(modifier = Modifier.weight(1f)) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ALBUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = album.releaseYear,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Album Title
                Text(
                    text = album.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Clickable Artist Link with Verified checkmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = onArtistClick != null) {
                        onArtistClick?.invoke(album.artist)
                    }
                ) {
                    Text(
                        text = album.artist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Artist",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Genre & Track count pill badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = album.genre,
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${album.trackCount.coerceAtLeast(album.tracks.size)} tracks",
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Play Album Circular Button (Visual consistency with Artist screen play FAB)
            if (album.tracks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isAlbumActive) SpotifyGreen else WhiteSmoke)
                        .clickable {
                            val target = album.tracks.firstOrNull()
                            if (target != null) {
                                onPlaySong(target, album.tracks)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAlbumActive && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = "Play Album",
                        tint = StormBlackBg,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row: Shuffle + Streaming Hubs deep links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            Button(
                onClick = {
                    if (album.tracks.isNotEmpty()) {
                        val shuffled = album.tracks.shuffled()
                        onPlaySong(shuffled.first(), shuffled)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Shuffle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Spotify Deep Link Chip
            val sampleSong = album.tracks.firstOrNull()
            if (sampleSong != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sampleSong.spotifyUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Spotify",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpotifyGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Spotify",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Apple Music Deep Link Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sampleSong.appleMusicUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Apple Music",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Apple Music",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Top 3 Featured Tracks Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    text = "Featured Album Tracks",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
            Text(
                text = "${album.tracks.size} tracks total",
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top 3 Tracklist items with Rank Badges
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            topThreeTracks.forEachIndexed { index, song ->
                val isFav = favoriteSongs.any { it.id == song.id }
                val isCurrent = currentPlayingId == song.id

                TopTrackItem(
                    rank = index + 1,
                    song = song,
                    isCurrent = isCurrent,
                    isFav = isFav,
                    onPlay = { onPlaySong(song, album.tracks) },
                    onOpenDetails = { onOpenSongDetails(song) },
                    onToggleFavorite = { onToggleFavorite(song) }
                )
            }
        }

        // 3. Complete Tracklist Toggle (Expandable for remaining tracks)
        if (remainingTracks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))

            // Expand / Collapse Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .clickable { isTracklistExpanded = !isTracklistExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTracklistExpanded) "Hide full tracklist" else "View complete tracklist (${remainingTracks.size} more)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpotifyGreen
                )
                Icon(
                    imageVector = if (isTracklistExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isTracklistExpanded) "Collapse" else "Expand",
                    tint = SpotifyGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isTracklistExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    remainingTracks.forEachIndexed { index, song ->
                        val isFav = favoriteSongs.any { it.id == song.id }
                        val isCurrent = currentPlayingId == song.id

                        TopTrackItem(
                            rank = index + 4,
                            song = song,
                            isCurrent = isCurrent,
                            isFav = isFav,
                            onPlay = { onPlaySong(song, album.tracks) },
                            onOpenDetails = { onOpenSongDetails(song) },
                            onToggleFavorite = { onToggleFavorite(song) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top Track row within Artist or Album rich cards.
 */
@Composable
private fun TopTrackItem(
    rank: Int,
    song: Song,
    isCurrent: Boolean,
    isFav: Boolean,
    onPlay: () -> Unit,
    onOpenDetails: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) SpotifyGreen.copy(alpha = 0.14f)
                else colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .clickable {
                onPlay()
                onOpenDetails()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = "#$rank",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) SpotifyGreen else colorScheme.onSurfaceVariant,
            modifier = Modifier.width(26.dp)
        )

        // Thumbnail
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and stats
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • 30s preview",
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Heart favorite toggle (Room DB)
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFav) WhiteSmoke else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
        }

        // Play Button
        IconButton(
            onClick = onPlay,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = if (isCurrent) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = SpotifyGreen,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

/**
 * Standard Song search result row with liquid glass effect and Room DB heart toggle.
 */
@Composable
private fun SearchSongRow(
    song: Song,
    index: Int,
    isCurrent: Boolean,
    isFav: Boolean,
    onPlay: () -> Unit,
    onOpenDetails: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(14.dp), elevation = 2.dp)
            .clickable {
                onPlay()
                onOpenDetails()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            AsyncImage(
                model = song.artworkUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
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

        // Heart favorite toggle button (Room DB)
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFav) WhiteSmoke else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Play button
        IconButton(
            onClick = onPlay,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isCurrent) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = SpotifyGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

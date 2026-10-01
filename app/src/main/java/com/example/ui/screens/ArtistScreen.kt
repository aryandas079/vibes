package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MusicaImage
import com.example.model.Artist
import com.example.model.Song
import com.example.ui.theme.*
import com.example.ui.theme.liquidGlassEffect

@Composable
fun ArtistScreen(
    artistName: String,
    topArtists: List<Artist>,
    artistSongs: List<Song>,
    isLoading: Boolean,
    favoriteSongs: List<Song>,
    followedArtists: List<Artist> = emptyList(),
    currentPlayingId: Long?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleFollow: ((Artist) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    currentArtist: Artist? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val isFollowing = remember(followedArtists, artistName) {
        followedArtists.any { it.name.equals(artistName, ignoreCase = true) }
    }

    val resolvedImageUrl = remember(artistName, currentArtist, topArtists, artistSongs) {
        val found = currentArtist?.imageUrl
            ?: topArtists.firstOrNull { it.name.equals(artistName, ignoreCase = true) }?.imageUrl
            ?: artistSongs.firstOrNull()?.artistImageUrl
        if (!found.isNullOrBlank() && !found.contains("33e9d300eb058aa35a4d3ec1590456c2")) {
            found
        } else {
            when {
                artistName.contains("taylor", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/cc2495870fe1a792ad0cdb05501ad5ec/500x500-000000-80-0-0.jpg"
                artistName.contains("rosé", ignoreCase = true) || artistName.contains("rose", ignoreCase = true) -> "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/c2/a9/23/c2a923ac-b382-e73b-91f4-013a8d5a0600/21UMGIM18155.rgb.jpg/600x600bb.jpg"
                artistName.contains("drake", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/1051e7fd110f9d3e5e88cdc69c5f227b/500x500-000000-80-0-0.jpg"
                artistName.contains("weeknd", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/581693b4724a7fcfa754455101e13a44/500x500-000000-80-0-0.jpg"
                artistName.contains("billie", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/8eab1a9a644889aabaca1e193e05f984/500x500-000000-80-0-0.jpg"
                artistName.contains("ed sheeran", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/d6bb84390641d8ae9118228d9544e53d/500x500-000000-80-0-0.jpg"
                artistName.contains("sabrina", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/4a9cdc7737e2a0e59b4917b47884b859/500x500-000000-80-0-0.jpg"
                artistName.contains("bad bunny", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/044a3f315b041864887a8dd8709e6926/500x500-000000-80-0-0.jpg"
                artistName.contains("bruno", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/90f0b5b11df4f87ee878f38569b5995b/500x500-000000-80-0-0.jpg"
                artistName.contains("gaga", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/7565262f7661b0d762621a8d69ba6f49/500x500-000000-80-0-0.jpg"
                artistName.contains("harry", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/1151dba9b3edc0633adf35b64c21713f/500x500-000000-80-0-0.jpg"
                artistName.contains("ariana", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/721d8fab84b315502de422b8d0901509/500x500-000000-80-0-0.jpg"
                artistName.contains("marshmello", ignoreCase = true) || artistName.contains("marshmallow", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/7990773a89df9f06fc2b871ad1de00bf/500x500-000000-80-0-0.jpg"
                artistName.contains("bastille", ignoreCase = true) || artistName.contains("bastile", ignoreCase = true) -> "https://cdn-images.dzcdn.net/images/artist/6b76e1f7a7bda7e7e41950d12c77702f/500x500-000000-80-0-0.jpg"
                else -> artistSongs.firstOrNull()?.artworkUrl ?: "https://cdn-images.dzcdn.net/images/artist/cc2495870fe1a792ad0cdb05501ad5ec/500x500-000000-80-0-0.jpg"
            }
        }
    }

    val displayArtist = currentArtist
        ?: topArtists.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
        ?: Artist(artistName, resolvedImageUrl, "Artist", "Verified Artist")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Cinematic Immersive Header with Photo Backdrop & Verified Avatar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    // Blurred / Atmospheric Image Backdrop
                    MusicaImage(
                        model = resolvedImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        titlePlaceholder = artistName
                    )

                    // Darkening and Vignette Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        StormBlackBg.copy(alpha = 0.45f),
                                        StormBlackSurface.copy(alpha = 0.8f),
                                        colorScheme.background
                                    )
                                )
                            )
                    )

                    // Top Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = WhiteSmoke
                            )
                        }

                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .size(40.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = WhiteSmoke,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Centered Artist Avatar & Details
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with glowing ring border & Verified badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(126.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, WhiteSmoke.copy(alpha = 0.6f), CircleShape)
                                    .liquidGlassEffect(shape = CircleShape, elevation = 8.dp)
                            ) {
                                MusicaImage(
                                    model = resolvedImageUrl,
                                    contentDescription = artistName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    titlePlaceholder = artistName
                                )
                            }

                            // Verified Checkmark Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(WhiteSmoke)
                                    .border(2.dp, StormBlackBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = StormBlackBg,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Verified Artist Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "VERIFIED ARTIST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmokeSoft,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Artist Name
                        Text(
                            text = artistName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = WhiteSmoke,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Monthly listeners count
                        Text(
                            text = "${displayArtist.topHitsCount} • 94M+ monthly listeners",
                            fontSize = 12.sp,
                            color = WhiteSmokeSoft
                        )
                    }
                }
            }

            // Action Controls Bar: Follow, Shuffle, Options, White Smoke Play FAB
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onToggleFollow?.invoke(displayArtist) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) StormBlackElevated else WhiteSmoke,
                                contentColor = if (isFollowing) WhiteSmoke else StormBlackBg
                            ),
                            shape = RoundedCornerShape(22.dp),
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                color = if (isFollowing) WhiteSmoke else StormBlackBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                if (artistSongs.isNotEmpty()) {
                                    val shuffled = artistSongs.shuffled()
                                    onPlaySong(shuffled.first(), shuffled)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* More */ },
                            modifier = Modifier
                                .size(40.dp)
                                .liquidGlassEffect(shape = CircleShape, elevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // White Smoke Circular Play FAB
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(WhiteSmoke)
                            .clickable {
                                artistSongs.firstOrNull()?.let { onPlaySong(it, artistSongs) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying && artistSongs.any { it.id == currentPlayingId }) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = "Play Artist",
                            tint = StormBlackBg,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // "Popular" Tracks Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Songs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    Text(
                        text = "${artistSongs.size} tracks",
                        fontSize = 12.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isLoading && artistSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SpotifyGreen)
                    }
                }
            } else if (artistSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading top tracks for $artistName...",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                itemsIndexed(artistSongs) { index, song ->
                    val isFav = favoriteSongs.any { it.id == song.id }
                    val isCurrent = currentPlayingId == song.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .liquidGlassEffect(
                                shape = RoundedCornerShape(14.dp),
                                elevation = if (isCurrent) 6.dp else 2.dp
                            )
                            .clickable {
                                onPlaySong(song, artistSongs)
                                onOpenSongDetails(song)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track Number
                        Text(
                            text = "${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) SpotifyGreen else colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(26.dp)
                        )

                        // Album Thumbnail
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            MusicaImage(
                                model = song.artworkUrl,
                                contentDescription = song.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                titlePlaceholder = song.title
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Info
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
                                text = "${song.album} • 30s preview",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Favorite Icon
                        IconButton(
                            onClick = { onToggleFavorite(song) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) WhiteSmoke else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = { onOpenSongDetails(song) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // "Popular Releases" Section
            if (artistSongs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Popular Releases",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(artistSongs.distinctBy { it.album }.take(8)) { song ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable {
                                        onPlaySong(song, artistSongs)
                                        onOpenSongDetails(song)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                ) {
                                    MusicaImage(
                                        model = song.artworkUrl,
                                        contentDescription = song.album,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        titlePlaceholder = song.album
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = song.album,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Album • ${song.releaseYear}",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

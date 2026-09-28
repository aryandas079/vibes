package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MusicaImage
import com.example.model.Artist
import com.example.model.GenreChartData
import com.example.model.Song
import com.example.ui.theme.*

@Composable
fun GenreDetailScreen(
    genreName: String,
    chartData: GenreChartData?,
    isLoading: Boolean,
    favoriteSongs: List<Song>,
    followedArtists: List<Artist>,
    currentPlayingId: Long?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenSongDetails: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onToggleFollowArtist: (Artist) -> Unit,
    onArtistClick: (String) -> Unit,
    onOpenLyrics: ((Song) -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    BackHandler {
        onBack()
    }

    val songs = chartData?.topSongs ?: emptyList()
    val heroSong = chartData?.heroSong ?: songs.firstOrNull()
    val remainingSongs = if (songs.isNotEmpty()) songs.drop(1) else emptyList()
    val topArtists = chartData?.topArtists ?: emptyList()

    val genreAccentColor = remember(genreName) {
        when (genreName.lowercase()) {
            "pop" -> GenrePopBg
            "hip-hop", "rap" -> GenreHipHopBg
            "rock" -> GenreRockBg
            "latin" -> GenreLatinBg
            "k-pop" -> GenreKPopBg
            "r&b" -> GenreRnBBg
            else -> SpotifyGreen
        }
    }

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
            // 1. Top Bar Navigation & Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                            .testTag("genre_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WhiteSmoke
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = genreAccentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GLOBAL REALTIME CHARTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = genreAccentColor,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "$genreName Top Charts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }

                    // Shuffle chart button
                    IconButton(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                val shuffled = songs.shuffled()
                                shuffled.firstOrNull()?.let { onPlaySong(it, shuffled) }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = WhiteSmoke,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Loading indicator if fetching chart
            if (isLoading && songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = genreAccentColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading real-time Billboard chart...",
                                fontSize = 13.sp,
                                color = WhiteSmokeMuted
                            )
                        }
                    }
                }
            }

            // 3. FIRST ONE: BIG HERO CARD (#1 Chart-Topper)
            if (heroSong != null) {
                item {
                    val isHeroCurrent = currentPlayingId == heroSong.id
                    val isHeroFav = favoriteSongs.any { it.id == heroSong.id }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Section Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFD700))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = StormBlackBg,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "NO. 1 WORLD CHART TOPPER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = StormBlackBg,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Billboard Global 200",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WhiteSmokeSoft
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Featured Hero Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassEffect(shape = RoundedCornerShape(24.dp), elevation = 10.dp)
                                .border(1.5.dp, genreAccentColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                .clickable {
                                    onPlaySong(heroSong, songs)
                                    onOpenSongDetails(heroSong)
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                // Cover Artwork with Glowing Backdrop & Rank Pill
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                ) {
                                    MusicaImage(
                                        model = heroSong.artworkUrl,
                                        contentDescription = heroSong.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        titlePlaceholder = heroSong.title
                                    )

                                    // Gradient overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        StormBlackBg.copy(alpha = 0.8f)
                                                    )
                                                )
                                            )
                                    )

                                    // #1 Rank Badge
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .align(Alignment.TopStart)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#1",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = StormBlackBg
                                        )
                                    }

                                    // Genre Tag
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StormBlackSurface.copy(alpha = 0.85f))
                                            .border(1.dp, WhiteSmokeSoft.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = heroSong.genre,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WhiteSmoke
                                        )
                                    }

                                    // Playing equalizer overlay if current
                                    if (isHeroCurrent && isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .align(Alignment.BottomEnd)
                                                .clip(CircleShape)
                                                .background(SpotifyGreen)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = "Playing",
                                                tint = StormBlackBg,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Song details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = heroSong.title,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${heroSong.artist} • ${heroSong.album}",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Heart button
                                    IconButton(
                                        onClick = { onToggleFavorite(heroSong) },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isHeroFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isHeroFav) SpotifyGreen else WhiteSmokeMuted,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons Row: Big Play Now Button & Lyrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            onPlaySong(heroSong, songs)
                                            onOpenSongDetails(heroSong)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SpotifyGreen,
                                            contentColor = StormBlackBg
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isHeroCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = StormBlackBg,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isHeroCurrent && isPlaying) "Now Playing" else "Play Chart-Topper",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Open Lyrics button
                                    Button(
                                        onClick = {
                                            onOpenLyrics?.invoke(heroSong) ?: onOpenSongDetails(heroSong)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.surfaceVariant,
                                            contentColor = colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Lyrics",
                                                tint = genreAccentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Lyrics",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 4. TOP BILLBOARD ARTISTS IN THIS GENRE
            if (topArtists.isNotEmpty()) {
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = genreAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Top Billboard Artists",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${topArtists.size} charted",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(topArtists) { artist ->
                                val isFollowed = followedArtists.any { it.name.equals(artist.name, ignoreCase = true) }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(96.dp)
                                        .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
                                        .clickable { onArtistClick(artist.name) }
                                        .padding(8.dp)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, WhiteSmoke.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            MusicaImage(
                                                model = artist.imageUrl,
                                                contentDescription = artist.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                titlePlaceholder = artist.name
                                            )
                                        }

                                        // Verified Badge
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(WhiteSmoke)
                                                .border(1.dp, StormBlackBg, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Verified",
                                                tint = StormBlackBg,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = artist.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Follow / Following button (working library addition)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isFollowed) colorScheme.surfaceVariant
                                                else SpotifyGreen.copy(alpha = 0.2f)
                                            )
                                            .clickable { onToggleFollowArtist(artist) }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isFollowed) "Following" else "+ Follow",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFollowed) WhiteSmokeSoft else SpotifyGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 5. BILLBOARD TOP CHARTS RANKED LIST (#2, #3, #4...)
            if (remainingSongs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Billboard Chart Ranking",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${songs.size} tracks",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(remainingSongs, key = { _, song -> song.id }) { index, song ->
                    val rank = index + 2 // Because hero is #1
                    val isCurrent = currentPlayingId == song.id
                    val isFav = favoriteSongs.any { it.id == song.id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .liquidGlassEffect(
                                shape = RoundedCornerShape(14.dp),
                                elevation = if (isCurrent) 6.dp else 2.dp
                            )
                            .clickable {
                                onPlaySong(song, songs)
                                onOpenSongDetails(song)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (rank) {
                                        2 -> Color(0xFFC0C0C0).copy(alpha = 0.25f)
                                        3 -> Color(0xFFCD7F32).copy(alpha = 0.25f)
                                        else -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$rank",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) SpotifyGreen else colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Artwork
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

                        Spacer(modifier = Modifier.width(10.dp))

                        // Track title & artist
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrent) SpotifyGreen else colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = song.artist,
                                    fontSize = 11.5.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(genreAccentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = song.genre,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = genreAccentColor
                                    )
                                }
                            }
                        }

                        // Heart favorite button
                        IconButton(
                            onClick = { onToggleFavorite(song) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) SpotifyGreen else WhiteSmokeMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Play Button
                        IconButton(
                            onClick = {
                                onPlaySong(song, songs)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent && isPlaying) SpotifyGreen else colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = if (isCurrent && isPlaying) StormBlackBg else WhiteSmoke,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

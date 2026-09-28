package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Album
import com.example.ui.components.AlbumDetailBottomSheet
import com.example.ui.components.AuthBottomSheet
import com.example.ui.components.EqualizerBottomSheet
import com.example.ui.components.GeminiMoodPlaylistSheet
import com.example.ui.components.MiniPlayer
import com.example.ui.components.PlayerTab
import com.example.ui.components.QueueDrawer
import com.example.ui.components.SpotifyEmbedDialog
import com.example.ui.screens.ArtistScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GenreDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LyricsScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LocalAppStyle
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.liquidGlassEffect
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicaApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showSplash by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var viewingArtistName by remember { mutableStateOf<String?>(null) }
    var viewingGenre by remember { mutableStateOf<String?>(null) }
    var playerTab by remember { mutableStateOf(PlayerTab.NOW_PLAYING) }

    val colorScheme = MaterialTheme.colorScheme

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isBuffering by viewModel.isBuffering.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val isLooping by viewModel.isLooping.collectAsStateWithLifecycle()
    val currentQueue by viewModel.currentQueue.collectAsStateWithLifecycle()
    val queueCurrentIndex by viewModel.queueCurrentIndex.collectAsStateWithLifecycle()
    val isQueueDrawerOpen by viewModel.isQueueDrawerOpen.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val featuredSong by viewModel.featuredSong.collectAsStateWithLifecycle()
    val trendingSongs by viewModel.trendingSongs.collectAsStateWithLifecycle()
    val categorySongs by viewModel.categorySongs.collectAsStateWithLifecycle()
    val recommendedSongs by viewModel.recommendedSongs.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val topArtists by viewModel.topArtists.collectAsStateWithLifecycle()
    val isHomescreenLoading by viewModel.isHomescreenLoading.collectAsStateWithLifecycle()

    val genreChartData by viewModel.genreChartData.collectAsStateWithLifecycle()
    val isGenreChartLoading by viewModel.isGenreChartLoading.collectAsStateWithLifecycle()

    val artistSongs by viewModel.artistSongs.collectAsStateWithLifecycle()
    val isArtistLoading by viewModel.isArtistLoading.collectAsStateWithLifecycle()
    val currentArtist by viewModel.currentArtist.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val matchedArtist by viewModel.matchedArtist.collectAsStateWithLifecycle()
    val matchedArtists by viewModel.matchedArtists.collectAsStateWithLifecycle()
    val matchedAlbum by viewModel.matchedAlbum.collectAsStateWithLifecycle()
    val matchedAlbums by viewModel.matchedAlbums.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val followedArtists by viewModel.followedArtists.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val featuredAlbums by viewModel.featuredAlbums.collectAsStateWithLifecycle()

    val deviceSongs by viewModel.deviceSongs.collectAsStateWithLifecycle()
    val isScanningDeviceFiles by viewModel.isScanningDeviceFiles.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()

    val discoveryRecommendations by viewModel.discoveryRecommendations.collectAsStateWithLifecycle()
    val isDiscoveryLoading by viewModel.isDiscoveryLoading.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val lyricsData by viewModel.lyricsData.collectAsStateWithLifecycle()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isSpotifyEmbedVisible by viewModel.isSpotifyEmbedVisible.collectAsStateWithLifecycle()

    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()

    var showAuthSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showMoodPlaylistSheet by remember { mutableStateOf(false) }
    var selectedAlbumForSheet by remember { mutableStateOf<Album?>(null) }
    val authSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spotifySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moodPlaylistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val albumSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val moodPlaylistState by viewModel.moodPlaylistState.collectAsStateWithLifecycle()

    val upcomingCount = if (queueCurrentIndex >= 0 && queueCurrentIndex < currentQueue.size - 1) {
        currentQueue.size - (queueCurrentIndex + 1)
    } else 0

    var showOfflineStatusBar by remember { mutableStateOf(false) }
    var offlineStatusBarColor by remember { mutableStateOf(Color(0xFFE57373)) }
    var offlineStatusBarText by remember { mutableStateOf("Offline Mode Active") }

    LaunchedEffect(isOfflineMode) {
        if (isOfflineMode) {
            offlineStatusBarColor = Color(0xFFFF9800) // Amber/Orange
            offlineStatusBarText = "Offline Mode Active • Playing Device Files"
            showOfflineStatusBar = true
        } else {
            if (showOfflineStatusBar) {
                offlineStatusBarColor = Color(0xFF1DB954) // Green
                offlineStatusBarText = "Connected to Internet"
                kotlinx.coroutines.delay(2500)
                showOfflineStatusBar = false
            }
        }
    }

    // Splash Screen matching Image 1
    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    Box(modifier = modifier.fillMaxSize().background(colorScheme.background)) {
        Scaffold(
            containerColor = colorScheme.background,
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // Docked MiniPlayer with Liquid Glass styling
                    if (!isNowPlayingExpanded && currentSong != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .liquidGlassEffect(shape = RoundedCornerShape(18.dp), elevation = 8.dp)
                        ) {
                            MiniPlayer(
                                song = currentSong,
                                isPlaying = isPlaying,
                                isBuffering = isBuffering,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.playNext() },
                                onPrevious = { viewModel.playPrevious() },
                                onSeek = { viewModel.seekTo(it) },
                                onExpand = { 
                                    playerTab = PlayerTab.NOW_PLAYING
                                    viewModel.openNowPlaying() 
                                },
                                onOpenQueue = { viewModel.openQueueDrawer() },
                                upcomingCount = upcomingCount
                            )
                        }
                    }

                    // Completely Unique Floating Neon Capsule Command Dock (Non-standard layout)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f), RoundedCornerShape(32.dp)),
                            shape = RoundedCornerShape(32.dp),
                            color = Color(0xFF12101F).copy(alpha = 0.95f),
                            tonalElevation = 12.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Nexus Tab (Home)
                                val isNexusSelected = selectedTab == 0 && viewingArtistName == null && viewingGenre == null
                                IconButton(
                                    onClick = {
                                        selectedTab = 0
                                        viewingArtistName = null
                                        viewingGenre = null
                                    },
                                    modifier = Modifier.size(44.dp).testTag("nav_nexus_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Nexus",
                                        tint = if (isNexusSelected) Color(0xFFC084FC) else Color(0xFF8B8F9F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Radar Tab (Search)
                                val isRadarSelected = selectedTab == 1 && viewingArtistName == null && viewingGenre == null
                                IconButton(
                                    onClick = {
                                        selectedTab = 1
                                        viewingArtistName = null
                                        viewingGenre = null
                                    },
                                    modifier = Modifier.size(44.dp).testTag("nav_radar_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Radar",
                                        tint = if (isRadarSelected) Color(0xFF22D3EE) else Color(0xFF8B8F9F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Vault Tab (Favorites)
                                val isVaultSelected = selectedTab == 2 && viewingArtistName == null && viewingGenre == null
                                IconButton(
                                    onClick = {
                                        selectedTab = 2
                                        viewingArtistName = null
                                        viewingGenre = null
                                    },
                                    modifier = Modifier.size(44.dp).testTag("nav_vault_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Vault",
                                        tint = if (isVaultSelected) Color(0xFFF472B6) else Color(0xFF8B8F9F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                // If viewing Artist page
                if (viewingArtistName != null) {
                    ArtistScreen(
                        artistName = viewingArtistName!!,
                        topArtists = topArtists,
                        artistSongs = artistSongs,
                        isLoading = isArtistLoading,
                        favoriteSongs = favoriteSongs,
                        followedArtists = followedArtists,
                        currentPlayingId = currentSong?.id,
                        isPlaying = isPlaying,
                        onPlaySong = { s, p -> viewModel.playSong(s, p) },
                        onOpenSongDetails = { s -> viewModel.openNowPlaying(s) },
                        onToggleFavorite = { s -> viewModel.toggleFavorite(s) },
                        onToggleFollow = { artist -> viewModel.toggleFollowArtist(artist) },
                        onBack = { viewingArtistName = null },
                        currentArtist = currentArtist
                    )
                } else if (viewingGenre != null) {
                    // If viewing Realtime Genre & Billboard Top Charts
                    GenreDetailScreen(
                        genreName = viewingGenre!!,
                        chartData = genreChartData,
                        isLoading = isGenreChartLoading,
                        favoriteSongs = favoriteSongs,
                        followedArtists = followedArtists,
                        currentPlayingId = currentSong?.id,
                        isPlaying = isPlaying,
                        onPlaySong = { s, p -> viewModel.playSong(s, p) },
                        onOpenSongDetails = { s -> viewModel.openNowPlaying(s) },
                        onToggleFavorite = { s -> viewModel.toggleFavorite(s) },
                        onToggleFollowArtist = { artist -> viewModel.toggleFollowArtist(artist) },
                        onArtistClick = { artistName ->
                            viewModel.loadArtistSongs(artistName)
                            viewingArtistName = artistName
                            viewingGenre = null
                        },
                        onOpenLyrics = { song ->
                            playerTab = PlayerTab.LYRICS
                            viewModel.openNowPlaying(song)
                        },
                        onBack = { viewingGenre = null }
                    )
                } else {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            featuredSong = featuredSong,
                            trendingSongs = trendingSongs,
                            categorySongs = categorySongs,
                            recommendedSongs = recommendedSongs,
                            selectedCategory = selectedCategory,
                            topArtists = topArtists,
                            favoriteSongs = favoriteSongs,
                            isLoading = isHomescreenLoading,
                            isPlaying = isPlaying,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onPlaySong = { song, playlist -> viewModel.playSong(song, playlist) },
                            onOpenSongDetails = { viewModel.openNowPlaying(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onArtistClick = { artistName ->
                                viewingArtistName = artistName
                                viewModel.loadArtistSongs(artistName)
                            },
                            onNavigateToSearch = { selectedTab = 1 },
                            onOpenGenre = { genreName ->
                                viewingGenre = genreName
                                viewModel.loadGenreChart(genreName)
                            },
                            historyItems = historyItems,
                            currentPlayingId = currentSong?.id,
                            discoveryRecommendations = discoveryRecommendations,
                            isDiscoveryLoading = isDiscoveryLoading,
                            onRefreshDiscovery = { viewModel.refreshGeminiDiscovery() },
                            onOpenLyrics = { song ->
                                playerTab = PlayerTab.LYRICS
                                viewModel.openNowPlaying(song)
                            },
                            themeMode = themeMode,
                            onToggleTheme = { viewModel.toggleTheme() },
                            userSession = userSession,
                            syncStatus = syncStatus,
                            onOpenAuth = { showAuthSheet = true },
                            deviceSongs = deviceSongs,
                            isOfflineMode = isOfflineMode,
                            isScanningDeviceFiles = isScanningDeviceFiles,
                            onSyncDeviceFiles = { viewModel.scanAndSyncDeviceAudio() },
                            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                            onOpenMoodPlaylistGenerator = { showMoodPlaylistSheet = true },
                            featuredAlbums = featuredAlbums,
                            onOpenAlbum = { album -> selectedAlbumForSheet = album }
                        )

                        1 -> SearchScreen(
                            query = searchQuery,
                            searchResults = searchResults,
                            catalogSongs = trendingSongs + recommendedSongs,
                            matchedArtist = matchedArtist,
                            matchedArtists = matchedArtists,
                            matchedAlbum = matchedAlbum,
                            matchedAlbums = matchedAlbums,
                            isSearching = isSearching,
                            currentPlayingId = currentSong?.id,
                            isPlaying = isPlaying,
                            favoriteSongs = favoriteSongs,
                            followedArtists = followedArtists,
                            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            onPlaySong = { song, playlist -> viewModel.playSong(song, playlist) },
                            onOpenSongDetails = { viewModel.openNowPlaying(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleFollowArtist = { viewModel.toggleFollowArtist(it) },
                            onArtistClick = { artistName ->
                                viewingArtistName = artistName
                                viewModel.loadArtistSongs(artistName)
                            }
                        )

                        2 -> FavoritesScreen(
                            favoriteSongs = favoriteSongs,
                            followedArtists = followedArtists,
                            historyItems = historyItems,
                            topArtists = topArtists,
                            playlists = playlists,
                            currentPlayingId = currentSong?.id,
                            isPlaying = isPlaying,
                            onPlaySong = { song, playlist -> viewModel.playSong(song, playlist) },
                            onOpenSongDetails = { viewModel.openNowPlaying(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onToggleFollowArtist = { viewModel.toggleFollowArtist(it) },
                            onArtistClick = { artistName ->
                                viewingArtistName = artistName
                                viewModel.loadArtistSongs(artistName)
                            },
                            deviceSongs = deviceSongs,
                            isScanningDeviceFiles = isScanningDeviceFiles,
                            onSyncDeviceFiles = { viewModel.scanAndSyncDeviceAudio() },
                            onOpenMoodPlaylistGenerator = { showMoodPlaylistSheet = true }
                        )
                    }
                }
            }
        }

        // Unified Full Screen Player with Tab-Based Navigation (Now Playing & Synced Lyrics)
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            val isCurrentFav = currentSong?.let { s -> favoriteSongs.any { it.id == s.id } } ?: false

            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                isLooping = isLooping,
                isFavorite = isCurrentFav,
                selectedTab = playerTab,
                onTabSelected = { playerTab = it },
                lyricsData = lyricsData,
                isLyricsLoading = isLyricsLoading,
                selectedLanguage = selectedLanguage,
                onSelectLanguage = { viewModel.setTargetLanguage(it) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekBy = { viewModel.seekBy(it) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleLoop = { viewModel.toggleLooping() },
                onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                onClose = { viewModel.closeNowPlaying() },
                onOpenQueue = { viewModel.openQueueDrawer() },
                onOpenSpotifyEmbed = { viewModel.toggleSpotifyEmbed(true) },
                onOpenLyrics = { playerTab = PlayerTab.LYRICS },
                onOpenEqualizer = { showEqualizerSheet = true },
                onArtistClick = { artistName ->
                    viewModel.closeNowPlaying()
                    viewModel.loadArtistSongs(artistName)
                    viewingArtistName = artistName
                }
            )
        }

        if (showEqualizerSheet) {
            EqualizerBottomSheet(
                playerManager = viewModel.playerManager,
                onDismiss = { showEqualizerSheet = false }
            )
        }

        if (showMoodPlaylistSheet) {
            GeminiMoodPlaylistSheet(
                uiState = moodPlaylistState,
                sheetState = moodPlaylistSheetState,
                onDismiss = {
                    showMoodPlaylistSheet = false
                    viewModel.clearMoodPlaylistState()
                },
                onGeneratePlaylist = { mood, activity, customPrompt ->
                    viewModel.generateMoodPlaylist(mood, activity, customPrompt)
                },
                onPlayPlaylistSequence = { playlist ->
                    viewModel.playMoodPlaylistSequence(playlist)
                },
                onPlayTrack = { song ->
                    viewModel.playSong(song)
                },
                onSaveToLibrary = { playlist ->
                    viewModel.saveMoodPlaylistToLibrary(playlist)
                }
            )
        }

        if (selectedAlbumForSheet != null) {
            AlbumDetailBottomSheet(
                album = selectedAlbumForSheet!!,
                isPlaying = isPlaying,
                currentPlayingId = currentSong?.id,
                favoriteSongIds = favoriteSongs.map { it.id }.toSet(),
                onPlaySong = { song, tracklist ->
                    viewModel.playSong(song, tracklist)
                },
                onPlayEntireAlbum = { album ->
                    if (album.tracks.isNotEmpty()) {
                        viewModel.playSong(album.tracks.first(), album.tracks)
                    }
                },
                onShuffleAlbum = { album ->
                    if (album.tracks.isNotEmpty()) {
                        val shuffled = album.tracks.shuffled()
                        viewModel.playSong(shuffled.first(), shuffled)
                    }
                },
                onToggleFavorite = { song ->
                    viewModel.toggleFavorite(song)
                },
                onDismiss = { selectedAlbumForSheet = null },
                sheetState = albumSheetState
            )
        }

        // Playback Queue Drawer accessible from Playback Bar & Now Playing Screen
        if (isQueueDrawerOpen) {
            QueueDrawer(
                currentSong = currentSong,
                queue = currentQueue,
                currentIndex = queueCurrentIndex,
                isPlaying = isPlaying,
                sheetState = queueSheetState,
                onDismiss = { viewModel.closeQueueDrawer() },
                onPlayTrackAt = { index -> viewModel.playTrackFromQueue(index) },
                onMoveUpcomingUp = { index -> viewModel.moveUpcomingTrackUp(index) },
                onMoveUpcomingDown = { index -> viewModel.moveUpcomingTrackDown(index) },
                onRemoveUpcoming = { index -> viewModel.removeUpcomingTrack(index) },
                onClearUpcoming = { viewModel.clearUpcomingQueue() },
                onAddRecommended = { viewModel.addRecommendedToQueue() }
            )
        }

        // Spotify Embed Modal Bottom Sheet
        if (isSpotifyEmbedVisible && currentSong != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleSpotifyEmbed(false) },
                sheetState = spotifySheetState,
                containerColor = colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                SpotifyEmbedDialog(
                    song = currentSong!!,
                    onDismiss = { viewModel.toggleSpotifyEmbed(false) }
                )
            }
        }

        // Firebase Auth & Google Sign-In Modal Bottom Sheet
        if (showAuthSheet) {
            AuthBottomSheet(
                sheetState = authSheetState,
                userSession = userSession,
                syncStatus = syncStatus,
                lastSyncTime = lastSyncTime,
                isLoading = isAuthLoading,
                errorMessage = authErrorMessage,
                onDismiss = {
                    showAuthSheet = false
                    viewModel.clearAuthError()
                },
                onSignInWithGoogle = { activity ->
                    viewModel.signInWithGoogle(activity)
                },
                onQuickSignInAsAryan = { email, name ->
                    viewModel.quickSignInAsAryan(email, name)
                },
                onSignOut = {
                    viewModel.signOut()
                },
                onSyncNow = {
                    viewModel.syncUserData()
                },
                favoritesCount = favoriteSongs.size,
                historyCount = historyItems.size
            )
        }

        // Floating Offline Status Bar Overlay
        AnimatedVisibility(
            visible = showOfflineStatusBar,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(offlineStatusBarColor)
                    .statusBarsPadding()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = offlineStatusBarText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

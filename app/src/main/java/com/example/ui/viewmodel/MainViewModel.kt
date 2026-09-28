package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.local.MusicaDatabase
import com.example.data.repository.MusicRepository
import com.example.model.Album
import com.example.model.AppThemeMode
import com.example.model.Artist
import com.example.model.DiscoveryRecommendation
import com.example.model.GeminiMoodPlaylist
import com.example.model.GeminiTrackSequence
import com.example.model.GenreChartData
import com.example.model.HistoryItem
import com.example.model.LyricsData
import com.example.model.Song
import com.example.model.SyncStatus
import com.example.model.UserSession
import com.example.player.AudioPlayerManager
import com.example.util.LyricsEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LyricsDisplayMode {
    ORIGINAL,
    BILINGUAL,
    TRANSLATED
}

sealed interface MoodPlaylistUiState {
    object Idle : MoodPlaylistUiState
    data class Generating(val step: String) : MoodPlaylistUiState
    data class Success(val playlist: GeminiMoodPlaylist) : MoodPlaylistUiState
    data class Error(val message: String) : MoodPlaylistUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicaDatabase.getInstance(application)
    val repository = MusicRepository(db.songDao())
    val playerManager = AudioPlayerManager(application)
    val authManager = FirebaseAuthManager(application)

    // User Authentication & Firestore Cloud Sync
    val userSession: StateFlow<UserSession?> = authManager.currentUser

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Player state delegated from player manager
    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val isBuffering: StateFlow<Boolean> = playerManager.isBuffering
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val isLooping: StateFlow<Boolean> = playerManager.isLooping
    val currentQueue: StateFlow<List<Song>> = playerManager.currentQueue
    val queueCurrentIndex: StateFlow<Int> = playerManager.currentIndex

    // Queue Drawer State
    private val _isQueueDrawerOpen = MutableStateFlow(false)
    val isQueueDrawerOpen: StateFlow<Boolean> = _isQueueDrawerOpen.asStateFlow()

    // Favorites from Room DB
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Followed Artists from Room DB
    val followedArtists: StateFlow<List<Artist>> = repository.followedArtists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Playlists from Room DB
    val playlists: StateFlow<List<com.example.data.local.PlaylistEntity>> = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createPlaylist(name: String, description: String = "", coverUrl: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description, coverUrl)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    // History from Room DB
    val historyItems: StateFlow<List<HistoryItem>> = repository.historyItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Appearance
    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Home Screen State
    private val _featuredSong = MutableStateFlow<Song?>(null)
    val featuredSong: StateFlow<Song?> = _featuredSong.asStateFlow()

    private val _trendingSongs = MutableStateFlow<List<Song>>(emptyList())
    val trendingSongs: StateFlow<List<Song>> = _trendingSongs.asStateFlow()

    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()

    // Gemini AI Song Discovery Recommendations based on listening history
    private val _discoveryRecommendations = MutableStateFlow<List<DiscoveryRecommendation>>(emptyList())
    val discoveryRecommendations: StateFlow<List<DiscoveryRecommendation>> = _discoveryRecommendations.asStateFlow()

    private val _isDiscoveryLoading = MutableStateFlow(false)
    val isDiscoveryLoading: StateFlow<Boolean> = _isDiscoveryLoading.asStateFlow()

    // Gemini Mood & Activity Track Sequence Generator
    private val _moodPlaylistState = MutableStateFlow<MoodPlaylistUiState>(MoodPlaylistUiState.Idle)
    val moodPlaylistState: StateFlow<MoodPlaylistUiState> = _moodPlaylistState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _categorySongs = MutableStateFlow<List<Song>>(emptyList())
    val categorySongs: StateFlow<List<Song>> = _categorySongs.asStateFlow()

    private val _topArtists = MutableStateFlow<List<Artist>>(emptyList())
    val topArtists: StateFlow<List<Artist>> = _topArtists.asStateFlow()

    private val _isHomescreenLoading = MutableStateFlow(true)
    val isHomescreenLoading: StateFlow<Boolean> = _isHomescreenLoading.asStateFlow()

    // Local Device Files & Offline Storage Sync
    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs: StateFlow<List<Song>> = _deviceSongs.asStateFlow()

    private val _isScanningDeviceFiles = MutableStateFlow(false)
    val isScanningDeviceFiles: StateFlow<Boolean> = _isScanningDeviceFiles.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Genre Top Charts State with #1 Big Hero Track
    private val _genreChartData = MutableStateFlow<GenreChartData?>(null)
    val genreChartData: StateFlow<GenreChartData?> = _genreChartData.asStateFlow()

    private val _isGenreChartLoading = MutableStateFlow(false)
    val isGenreChartLoading: StateFlow<Boolean> = _isGenreChartLoading.asStateFlow()

    // Artist Detail Discography State
    private val _artistSongs = MutableStateFlow<List<Song>>(emptyList())
    val artistSongs: StateFlow<List<Song>> = _artistSongs.asStateFlow()

    private val _currentArtist = MutableStateFlow<Artist?>(null)
    val currentArtist: StateFlow<Artist?> = _currentArtist.asStateFlow()

    private val _isArtistLoading = MutableStateFlow(false)
    val isArtistLoading: StateFlow<Boolean> = _isArtistLoading.asStateFlow()

    // Search State with Rich Artist and Album Matches
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(
        listOf("The Weeknd", "Taylor Swift", "Sabrina Carpenter", "Billie Eilish")
    )
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    fun recordSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            val current = _searchHistory.value.toMutableList()
            current.removeAll { it.equals(trimmed, ignoreCase = true) }
            current.add(0, trimmed)
            _searchHistory.value = current.take(12)
            refreshGeminiDiscovery()
        }
    }

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _matchedArtist = MutableStateFlow<Artist?>(null)
    val matchedArtist: StateFlow<Artist?> = _matchedArtist.asStateFlow()

    private val _matchedArtists = MutableStateFlow<List<Artist>>(emptyList())
    val matchedArtists: StateFlow<List<Artist>> = _matchedArtists.asStateFlow()

    private val _matchedAlbum = MutableStateFlow<Album?>(null)
    val matchedAlbum: StateFlow<Album?> = _matchedAlbum.asStateFlow()

    private val _matchedAlbums = MutableStateFlow<List<Album>>(emptyList())
    val matchedAlbums: StateFlow<List<Album>> = _matchedAlbums.asStateFlow()

    private val _featuredAlbums = MutableStateFlow<List<Album>>(emptyList())
    val featuredAlbums: StateFlow<List<Album>> = _featuredAlbums.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
    }

    fun clearSelectedAlbum() {
        _selectedAlbum.value = null
    }

    fun playAlbum(album: Album) {
        if (album.tracks.isNotEmpty()) {
            playerManager.playSong(album.tracks.first(), album.tracks)
        }
    }

    fun shuffleAlbum(album: Album) {
        if (album.tracks.isNotEmpty()) {
            val shuffled = album.tracks.shuffled()
            playerManager.playSong(shuffled.first(), shuffled)
        }
    }

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Lyrics & Translation State
    private val _lyricsData = MutableStateFlow<LyricsData?>(null)
    val lyricsData: StateFlow<LyricsData?> = _lyricsData.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Original")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _lyricsDisplayMode = MutableStateFlow(LyricsDisplayMode.BILINGUAL)
    val lyricsDisplayMode: StateFlow<LyricsDisplayMode> = _lyricsDisplayMode.asStateFlow()

    private val _isRomanizationEnabled = MutableStateFlow(true)
    val isRomanizationEnabled: StateFlow<Boolean> = _isRomanizationEnabled.asStateFlow()

    // Navigation & Modal Sheets
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isSpotifyEmbedVisible = MutableStateFlow(false)
    val isSpotifyEmbedVisible: StateFlow<Boolean> = _isSpotifyEmbedVisible.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHomeScreenData()
        // seedFollowedArtistsIfEmpty() - Only added if followed by user
        // scanAndSyncDeviceAudio() - Only shown if explicitly added/scanned by user

        // Automatically sync lyrics whenever playerManager changes song
        viewModelScope.launch {
            playerManager.currentSong.collect { song ->
                if (song != null) {
                    fetchLyricsForSong(song)
                }
            }
        }

        // Dynamically compute recommendations & top artists when history changes
        viewModelScope.launch {
            historyItems.collect { history ->
                val songs = history.map { it.song }
                val recs = repository.getRecommendations(songs)
                _recommendedSongs.value = recs
                refreshDynamicTopArtists()
                // Refresh AI Discovery based on updated listening history
                if (_discoveryRecommendations.value.isEmpty()) {
                    refreshGeminiDiscovery()
                }
            }
        }

        // Dynamically refresh homepage artists when favorites or followed change
        viewModelScope.launch {
            favoriteSongs.collect {
                refreshDynamicTopArtists()
            }
        }

        viewModelScope.launch {
            followedArtists.collect {
                refreshDynamicTopArtists()
            }
        }

        // Listen for authentication changes to bind active user and trigger cloud sync
        viewModelScope.launch {
            userSession.collect { session ->
                if (session != null) {
                    repository.activeUserId = session.uid
                    syncUserData()
                } else {
                    repository.activeUserId = null
                }
            }
        }
    }

    fun scanAndSyncDeviceAudio() {
        viewModelScope.launch {
            _isScanningDeviceFiles.value = true
            try {
                val scanned = com.example.util.DeviceAudioScanner.scanDeviceAudioFiles(getApplication())
                _deviceSongs.value = scanned
            } catch (e: Exception) {
                // Ignore
            } finally {
                _isScanningDeviceFiles.value = false
            }
        }
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        if (_isOfflineMode.value && _deviceSongs.value.isEmpty()) {
            scanAndSyncDeviceAudio()
        }
    }

    fun loadGenreChart(genreName: String) {
        viewModelScope.launch {
            _isGenreChartLoading.value = true
            try {
                val chart = repository.getGenreChartData(genreName)
                _genreChartData.value = chart
            } catch (e: Exception) {
                // Ignore
            } finally {
                _isGenreChartLoading.value = false
            }
        }
    }

    fun refreshDynamicTopArtists() {
        viewModelScope.launch {
            try {
                val dynamic = repository.getDynamicTopArtists(
                    historySongs = historyItems.value.map { it.song },
                    favoriteSongs = favoriteSongs.value,
                    followedArtists = followedArtists.value
                )
                if (dynamic.isNotEmpty()) {
                    _topArtists.value = dynamic
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun refreshGeminiDiscovery() {
        viewModelScope.launch {
            _isDiscoveryLoading.value = true
            try {
                val discovery = repository.getGeminiDiscoveryRecommendations(
                    searchHistory = _searchHistory.value,
                    history = historyItems.value,
                    favorites = favoriteSongs.value
                )
                _discoveryRecommendations.value = discovery
            } catch (e: Exception) {
                // Fallback to recommended songs if any unexpected failure
            } finally {
                _isDiscoveryLoading.value = false
            }
        }
    }

    fun generateMoodPlaylist(mood: String, activity: String, customPrompt: String = "") {
        viewModelScope.launch {
            _moodPlaylistState.value = MoodPlaylistUiState.Generating("Consulting Gemini 3.5 Flash...")
            delay(350)
            _moodPlaylistState.value = MoodPlaylistUiState.Generating("Calibrating emotional arc & BPM progression...")
            delay(350)
            _moodPlaylistState.value = MoodPlaylistUiState.Generating("Sequencing harmonic transitions...")
            try {
                val userSongs = (historyItems.value.map { it.song } + favoriteSongs.value).distinctBy { it.id }
                val playlist = repository.generateMoodPlaylistSequence(
                    mood = mood,
                    activity = activity,
                    customPrompt = customPrompt,
                    userTasteSongs = userSongs
                )
                _moodPlaylistState.value = MoodPlaylistUiState.Success(playlist)
            } catch (e: Exception) {
                _moodPlaylistState.value = MoodPlaylistUiState.Error(e.message ?: "Failed to generate playlist sequence")
            }
        }
    }

    fun playMoodPlaylistSequence(playlist: GeminiMoodPlaylist) {
        val playableSongs = playlist.tracks.mapNotNull { it.resolvedSong }
        if (playableSongs.isNotEmpty()) {
            playerManager.playSong(playableSongs.first(), playableSongs)
        }
    }

    fun saveMoodPlaylistToLibrary(playlist: GeminiMoodPlaylist, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val coverUrl = playlist.tracks.firstOrNull()?.resolvedSong?.artworkUrl ?: ""
            val playlistId = repository.createPlaylist(
                name = playlist.title,
                description = "${playlist.narrativeArc} (${playlist.mood} • ${playlist.activity})",
                coverUrl = coverUrl
            )
            for (track in playlist.tracks) {
                track.resolvedSong?.let { song ->
                    repository.addSongToPlaylist(playlistId, song)
                }
            }
            onSaved()
        }
    }

    fun clearMoodPlaylistState() {
        _moodPlaylistState.value = MoodPlaylistUiState.Idle
    }

    fun loadHomeScreenData() {
        viewModelScope.launch {
            _isHomescreenLoading.value = true
            _topArtists.value = repository.getTopArtists()

            val trending = repository.getTrendingHits()
            _trendingSongs.value = trending
            _categorySongs.value = trending
            _featuredSong.value = trending.firstOrNull()

            _featuredAlbums.value = repository.getFeaturedAlbums()

            val recs = repository.getRecommendations(historyItems.value.map { it.song })
            _recommendedSongs.value = recs

            refreshGeminiDiscovery()

            _isHomescreenLoading.value = false
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = AppThemeMode.DARK
    }

    fun toggleTheme() {
        _themeMode.value = AppThemeMode.DARK
    }

    fun removeFromHistory(historyId: Long) {
        viewModelScope.launch {
            repository.removeFromHistory(historyId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            // Refresh recommendations to trending hits
            _recommendedSongs.value = repository.getTrendingHits()
        }
    }

    fun loadArtistSongs(artistName: String) {
        viewModelScope.launch {
            _isArtistLoading.value = true
            // Load artist details with verified profile picture
            val artistDetails = repository.getArtistDetails(artistName)
            _currentArtist.value = artistDetails
            val songs = repository.getArtistSongs(artistName)
            _artistSongs.value = songs
            _isArtistLoading.value = false
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isHomescreenLoading.value = true
            if (category == "All") {
                _categorySongs.value = _trendingSongs.value
            } else {
                _categorySongs.value = repository.getSongsByCategory(category)
            }
            _isHomescreenLoading.value = false
        }
    }

    private fun seedFollowedArtistsIfEmpty() {
        viewModelScope.launch {
            delay(500)
            val current = followedArtists.value
            if (current.isEmpty()) {
                val seed = repository.getTopArtists().take(3)
                seed.forEach { artist ->
                    repository.followArtist(artist)
                }
            }
        }
    }

    fun toggleFollowArtist(artist: Artist) {
        viewModelScope.launch {
            val isFollowed = followedArtists.value.any { it.name.equals(artist.name, ignoreCase = true) }
            if (isFollowed) {
                repository.unfollowArtist(artist.name)
            } else {
                repository.followArtist(artist)
            }
            // Update matched artist if currently viewed
            _matchedArtist.value?.let { current ->
                if (current.name.equals(artist.name, ignoreCase = true)) {
                    _matchedArtist.value = current.copy(isFollowed = !isFollowed)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _matchedArtist.value = null
            _matchedArtists.value = emptyList()
            _matchedAlbum.value = null
            _matchedAlbums.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(280) // Debounce
            _isSearching.value = true
            val results = repository.searchSongs(query)
            _searchResults.value = results

            val trimmed = query.trim()
            if (trimmed.length >= 3) {
                val current = _searchHistory.value.toMutableList()
                current.removeAll { it.equals(trimmed, ignoreCase = true) }
                current.add(0, trimmed)
                _searchHistory.value = current.take(12)
            }
            val allTopArtists = repository.getTopArtists()
            val directTopMatches = allTopArtists.filter {
                it.name.contains(trimmed, ignoreCase = true) || trimmed.contains(it.name, ignoreCase = true)
            }

            val matchingArtistNames = results.map { it.artist }.distinct().filter { artistName ->
                directTopMatches.none { it.name.equals(artistName, ignoreCase = true) } &&
                (artistName.contains(trimmed, ignoreCase = true) || trimmed.contains(artistName, ignoreCase = true))
            }

            val additionalArtists = matchingArtistNames.map { repository.getArtistDetails(it) }
            val combinedArtists = (directTopMatches + additionalArtists).distinctBy { it.name.lowercase() }

            val richMatchedArtists = combinedArtists.map { candidate ->
                val artistSongs = repository.getArtistSongs(candidate.name).ifEmpty {
                    results.filter { it.artist.equals(candidate.name, ignoreCase = true) }
                }
                val isFollowed = followedArtists.value.any { it.name.equals(candidate.name, ignoreCase = true) }
                candidate.copy(
                    isFollowed = isFollowed,
                    topSongs = if (artistSongs.isNotEmpty()) artistSongs else candidate.topSongs
                )
            }

            _matchedArtists.value = richMatchedArtists
            _matchedArtist.value = richMatchedArtists.firstOrNull()

            // 2. Detect and construct Matched Album Cards with Cover Art, Top 3 Featured Songs + Complete Tracklist
            val extractedAlbums = mutableListOf<Album>()
            val groupedByAlbum = results.filter { it.album.isNotBlank() }.groupBy { it.album }
            groupedByAlbum.forEach { (albumName, songs) ->
                val firstSong = songs.first()
                val isDirectMatch = albumName.contains(trimmed, ignoreCase = true) || trimmed.contains(albumName, ignoreCase = true)
                val albumObj = Album(
                    id = firstSong.id,
                    title = albumName,
                    artist = firstSong.artist,
                    artworkUrl = firstSong.artworkUrl,
                    releaseYear = firstSong.releaseYear,
                    genre = firstSong.genre,
                    trackCount = songs.size,
                    tracks = songs,
                    topFeaturedSongs = songs.take(3)
                )
                if (isDirectMatch) {
                    extractedAlbums.add(0, albumObj)
                } else {
                    extractedAlbums.add(albumObj)
                }
            }

            _matchedAlbums.value = extractedAlbums
            _matchedAlbum.value = extractedAlbums.firstOrNull()

            _isSearching.value = false
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val effectivePlaylist = if (playlist.isNotEmpty()) {
            playlist
        } else {
            val fallback = _trendingSongs.value.ifEmpty { _recommendedSongs.value }
            if (fallback.any { it.id == song.id }) fallback else (listOf(song) + fallback)
        }
        playerManager.playSong(song, effectivePlaylist)
        // Add to history so homescreen recommendations are dynamically updated
        viewModelScope.launch {
            repository.addToHistory(song)
        }
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        skipToNext()
    }

    fun playPrevious() {
        skipToPrevious()
    }

    fun skipToNext() {
        playerManager.playNext()
        val nextSong = playerManager.currentSong.value
        if (nextSong != null) {
            viewModelScope.launch {
                repository.addToHistory(nextSong)
            }
        }
    }

    fun skipToPrevious() {
        playerManager.playPrevious()
        val prevSong = playerManager.currentSong.value
        if (prevSong != null) {
            viewModelScope.launch {
                repository.addToHistory(prevSong)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun seekBy(deltaMs: Long) {
        playerManager.seekBy(deltaMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun toggleLooping() {
        playerManager.toggleLoop()
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFav = favoriteSongs.value.any { it.id == song.id }
            if (isFav) {
                repository.removeFavorite(song.id)
            } else {
                val cached = _lyricsData.value?.plainLyrics
                repository.toggleFavorite(song, cached)
            }
        }
    }

    fun openNowPlaying(song: Song? = null) {
        if (song != null && currentSong.value?.id != song.id) {
            playSong(song)
        }
        _isNowPlayingExpanded.value = true
    }

    fun setTargetLanguage(language: String) {
        setLanguage(language)
    }

    fun closeNowPlaying() {
        _isNowPlayingExpanded.value = false
    }

    // Queue Drawer & Session Management Actions
    fun openQueueDrawer() {
        _isQueueDrawerOpen.value = true
    }

    fun closeQueueDrawer() {
        _isQueueDrawerOpen.value = false
    }

    fun toggleQueueDrawer() {
        _isQueueDrawerOpen.value = !_isQueueDrawerOpen.value
    }

    fun reorderUpcomingTracks(fromIndex: Int, toIndex: Int) {
        playerManager.reorderUpcoming(fromIndex, toIndex)
    }

    fun moveUpcomingTrackUp(index: Int) {
        playerManager.moveUpcomingUp(index)
    }

    fun moveUpcomingTrackDown(index: Int) {
        playerManager.moveUpcomingDown(index)
    }

    fun removeUpcomingTrack(index: Int) {
        playerManager.removeUpcoming(index)
    }

    fun removeTrackFromQueue(songId: Long) {
        playerManager.removeTrackById(songId)
    }

    fun clearUpcomingQueue() {
        playerManager.clearUpcoming()
    }

    fun playTrackFromQueue(index: Int) {
        playerManager.playTrackAtQueueIndex(index)
    }

    fun addToQueue(song: Song, playNext: Boolean = false) {
        playerManager.addToQueue(song, playNext)
    }

    fun addRecommendedToQueue() {
        val recs = _recommendedSongs.value.ifEmpty { _trendingSongs.value }
        val currentIds = currentQueue.value.map { it.id }.toSet()
        val candidates = recs.filter { it.id !in currentIds }.take(5)
        val toAdd = if (candidates.isNotEmpty()) candidates else recs.take(5)
        if (toAdd.isNotEmpty()) {
            playerManager.addAllToQueue(toAdd)
        }
    }

    fun toggleSpotifyEmbed(visible: Boolean? = null) {
        _isSpotifyEmbedVisible.value = visible ?: !_isSpotifyEmbedVisible.value
    }

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
        applyLiveTranslationToCurrentLyrics(language)
    }

    fun setLyricsDisplayMode(mode: LyricsDisplayMode) {
        _lyricsDisplayMode.value = mode
    }

    fun toggleRomanization() {
        _isRomanizationEnabled.value = !_isRomanizationEnabled.value
    }

    private fun fetchLyricsForSong(song: Song) {
        viewModelScope.launch {
            _isLyricsLoading.value = true
            val lyrics = repository.getLyricsForSong(song)
            _lyricsData.value = lyrics
            _isLyricsLoading.value = false
            applyLiveTranslationToCurrentLyrics(_selectedLanguage.value)
        }
    }

    private fun applyLiveTranslationToCurrentLyrics(targetLang: String) {
        val current = _lyricsData.value ?: return
        if (targetLang == "Original") {
            val updatedLines = current.syncedLines.map { line ->
                line.copy(
                    translation = null,
                    romanized = if (_isRomanizationEnabled.value) LyricsEngine.romanizeIfApplicable(line.text) else null
                )
            }
            _lyricsData.value = current.copy(syncedLines = updatedLines)
            return
        }

        val cacheKey = "${current.songId}_$targetLang"
        val cached = LyricsEngine.getCachedTranslation(cacheKey)

        if (cached != null) {
            val updatedLines = current.syncedLines.map { line ->
                line.copy(
                    translation = cached[line.text],
                    romanized = if (_isRomanizationEnabled.value) LyricsEngine.romanizeIfApplicable(line.text) else null
                )
            }
            _lyricsData.value = current.copy(syncedLines = updatedLines)
            return
        }

        viewModelScope.launch {
            val translationMap = mutableMapOf<String, String>()
            val updatedLines = current.syncedLines.map { line ->
                val trans = LyricsEngine.translateLyricLine(line.text, targetLang)
                translationMap[line.text] = trans
                line.copy(
                    translation = trans,
                    romanized = if (_isRomanizationEnabled.value) LyricsEngine.romanizeIfApplicable(line.text) else null
                )
            }
            LyricsEngine.saveTranslation(cacheKey, translationMap)
            _lyricsData.value = current.copy(syncedLines = updatedLines)
        }
    }

    // Authentication & Firestore Sync Actions
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authManager.signInWithGoogle(activity)
            result.onSuccess { session ->
                _isAuthLoading.value = false
                repository.activeUserId = session.uid
                syncUserData()
            }.onFailure { error ->
                _isAuthLoading.value = false
                _authErrorMessage.value = error.message ?: "Google sign in failed"
            }
        }
    }

    fun quickSignInAsAryan(email: String = "aryandas.dev@gmail.com", name: String = "Aryan Das") {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authManager.quickSignInAsAryan(email, name)
            result.onSuccess { session ->
                _isAuthLoading.value = false
                repository.activeUserId = session.uid
                syncUserData()
            }.onFailure { error ->
                _isAuthLoading.value = false
                _authErrorMessage.value = error.message ?: "Sign in failed"
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authManager.signInWithEmail(email, pass)
            result.onSuccess { session ->
                _isAuthLoading.value = false
                repository.activeUserId = session.uid
                syncUserData()
            }.onFailure { error ->
                _isAuthLoading.value = false
                _authErrorMessage.value = error.message ?: "Sign in failed"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            repository.activeUserId = null
            _syncStatus.value = SyncStatus.IDLE
            _lastSyncTime.value = null
        }
    }

    fun syncUserData() {
        val user = userSession.value ?: return
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            try {
                repository.firestoreSync.saveUserProfile(user)
                val count = repository.syncWithFirestore(user.uid)
                _syncStatus.value = SyncStatus.SYNCED
                val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                _lastSyncTime.value = "Synced with Firestore at $now ($count favorites)"
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR
            }
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

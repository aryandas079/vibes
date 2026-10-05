package com.example.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioPlayerManager(private val context: Context) {

    init {
        MediaPlaybackService.activePlayerManager = this
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var resolveJob: Job? = null
    private var watchdogJob: Job? = null

    // Meaningful listening history tracking
    var onMeaningfulListen: ((Song) -> Unit)? = null
    private var hasRecordedListen = false

    private var androidEqualizer: android.media.audiofx.Equalizer? = null
    var isEqualizerEnabled: Boolean = true
        set(value) {
            field = value
            androidEqualizer?.enabled = value
        }

    fun getNumberOfBands(): Short {
        return androidEqualizer?.numberOfBands ?: 5
    }

    fun getBandLevelRange(): ShortArray {
        return androidEqualizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }

    fun getBandLevel(band: Short): Short {
        return try {
            androidEqualizer?.getBandLevel(band) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            androidEqualizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun getCenterFreq(band: Short): Int {
        return try {
            androidEqualizer?.getCenterFreq(band) ?: 1000
        } catch (e: Exception) {
            1000
        }
    }

    fun getPresetNames(): List<String> {
        val eq = androidEqualizer ?: return listOf("Flat", "Rock", "Pop", "Jazz", "Classical", "Bass Boost")
        val count = eq.numberOfPresets
        val list = mutableListOf<String>()
        for (i in 0 until count) {
            try {
                list.add(eq.getPresetName(i.toShort()))
            } catch (e: Exception) {
                list.add("Preset $i")
            }
        }
        if (list.isEmpty()) {
            return listOf("Flat", "Rock", "Pop", "Jazz", "Classical", "Bass Boost")
        }
        return list
    }

    fun usePreset(preset: Short) {
        try {
            androidEqualizer?.usePreset(preset)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(30000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private var currentPlaylist = mutableListOf<Song>()
    private var currentIndexInternal = -1

    private fun releaseCurrentMediaPlayer() {
        val player = mediaPlayer ?: return
        mediaPlayer = null
        try {
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            player.release()
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        if (playlist.isNotEmpty()) {
            currentPlaylist = playlist.toMutableList()
            currentIndexInternal = currentPlaylist.indexOfFirst { it.id == song.id }
            if (currentIndexInternal == -1) {
                currentPlaylist.add(0, song)
                currentIndexInternal = 0
            }
        } else if (_currentSong.value?.id != song.id) {
            currentPlaylist = mutableListOf(song)
            currentIndexInternal = 0
        }
        _currentQueue.value = currentPlaylist.toList()
        _currentIndex.value = currentIndexInternal

        // If the exact same song is already active and prepared, simply resume playback
        if (_currentSong.value?.id == song.id && mediaPlayer != null) {
            if (!_isPlaying.value) {
                try {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                    startProgressTicker()
                    startBackgroundService()
                } catch (e: Exception) {
                    // Fallthrough to reload if corrupted
                }
            }
            return
        }

        // Cancel pending background fetch/watchdog jobs immediately
        resolveJob?.cancel()
        resolveJob = null
        watchdogJob?.cancel()
        watchdogJob = null
        stopProgressTicker()
        hasRecordedListen = false

        // Stop, reset, and release old player immediately so songs NEVER overlap
        releaseCurrentMediaPlayer()

        _currentSong.value = song
        _currentPositionMs.value = 0L
        _isBuffering.value = true

        val previewUrl = song.previewUrl
        if (previewUrl.isNullOrEmpty()) {
            resolveAndPlay(song)
            return
        }

        playUrl(previewUrl, song, retryWithItunes = true)
    }

    private fun playUrl(url: String, song: Song, retryWithItunes: Boolean) {
        try {
            watchdogJob?.cancel()
            val secureUrl = if (url.startsWith("http://")) url.replace("http://", "https://") else url

            // Guard: don't start preparing if user already picked another song
            if (_currentSong.value?.id != song.id) return

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                try {
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (secureUrl.startsWith("http://") || secureUrl.startsWith("https://")) {
                    setDataSource(context, Uri.parse(secureUrl))
                } else {
                    setDataSource(secureUrl)
                }
                setOnPreparedListener { mp ->
                    watchdogJob?.cancel()
                    // Discard if the user navigated to another song while this one was buffering
                    if (_currentSong.value?.id != song.id) {
                        try {
                            mp.stop()
                            mp.reset()
                            mp.release()
                        } catch (e: Exception) {}
                        return@setOnPreparedListener
                    }

                    _isBuffering.value = false
                    _durationMs.value = mp.duration.toLong().coerceAtLeast(30000L)
                    try {
                        val sessionId = mp.audioSessionId
                        androidEqualizer?.release()
                        androidEqualizer = android.media.audiofx.Equalizer(0, sessionId).apply {
                            enabled = isEqualizerEnabled
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        mp.setVolume(1.0f, 1.0f)
                        mp.start()
                        _isPlaying.value = true
                        startProgressTicker()
                        startBackgroundService()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isPlaying.value = true
                        startProgressTicker()
                        startBackgroundService()
                    }
                }
                setOnCompletionListener {
                    // Record completion as meaningful listen
                    if (!hasRecordedListen && _currentSong.value?.id == song.id) {
                        hasRecordedListen = true
                        onMeaningfulListen?.invoke(song)
                    }

                    if (_isLooping.value) {
                        it.seekTo(0)
                        it.start()
                    } else {
                        playNext()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    watchdogJob?.cancel()
                    if (_currentSong.value?.id == song.id) {
                        if (retryWithItunes) {
                            resolveAndPlay(song)
                        } else {
                            _isBuffering.value = false
                            _durationMs.value = song.durationMs.coerceAtLeast(30000L)
                            _isPlaying.value = true
                            startProgressTicker()
                        }
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player

            watchdogJob = scope.launch(Dispatchers.Main) {
                delay(4000L)
                if (_isBuffering.value && _currentSong.value?.id == song.id) {
                    if (retryWithItunes) {
                        releaseCurrentMediaPlayer()
                        resolveAndPlay(song)
                    } else {
                        _isBuffering.value = false
                        _durationMs.value = song.durationMs.coerceAtLeast(30000L)
                        _isPlaying.value = true
                        startProgressTicker()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (retryWithItunes && _currentSong.value?.id == song.id) {
                resolveAndPlay(song)
            } else {
                _isBuffering.value = false
                _durationMs.value = song.durationMs.coerceAtLeast(30000L)
                _isPlaying.value = true
                startProgressTicker()
            }
        }
    }

    private fun resolveAndPlay(song: Song) {
        resolveJob?.cancel()
        resolveJob = scope.launch(Dispatchers.IO) {
            var resolvedUrl: String? = null
            try {
                val cleanTitle = song.title.replace(Regex("(?i)\\(.*\\)|\\[.*\\]|feat\\..*|ft\\..*"), "").trim()
                val primaryArtist = song.artist.split("&", ",", "feat.", "ft.", "and", "X", "x").firstOrNull()?.trim() ?: song.artist

                val searchQueries = listOf(
                    "$cleanTitle $primaryArtist",
                    "$cleanTitle ${song.artist}",
                    cleanTitle
                ).distinct()

                // 1. Try iTunes search across countries (IN for Indian/Bollywood, US for western, etc.)
                for (q in searchQueries) {
                    if (!resolvedUrl.isNullOrBlank()) break
                    for (country in listOf("IN", "US", "GB")) {
                        if (!resolvedUrl.isNullOrBlank()) break
                        try {
                            val itunes = com.example.data.remote.NetworkClient.itunesApi.searchSongs(term = q, country = country, limit = 5)
                            val match = itunes.results.firstOrNull { item ->
                                val itemTitle = item.trackName ?: ""
                                itemTitle.contains(cleanTitle, ignoreCase = true) || cleanTitle.contains(itemTitle, ignoreCase = true)
                            } ?: itunes.results.firstOrNull()
                            resolvedUrl = match?.previewUrl
                        } catch (e: Exception) {}
                    }
                }

                // 2. Try Deezer track search
                if (resolvedUrl.isNullOrBlank()) {
                    for (q in searchQueries) {
                        if (!resolvedUrl.isNullOrBlank()) break
                        try {
                            val deezer = com.example.data.remote.NetworkClient.deezerApi.searchTracks(q, limit = 5)
                            val match = deezer.data.firstOrNull { item ->
                                val itemTitle = item.title ?: ""
                                itemTitle.contains(cleanTitle, ignoreCase = true) || cleanTitle.contains(itemTitle, ignoreCase = true)
                            } ?: deezer.data.firstOrNull()
                            resolvedUrl = match?.preview
                        } catch (e: Exception) {}
                    }
                }
            } catch (e: Exception) {}

            withContext(Dispatchers.Main) {
                // Ensure this song is still the active choice
                if (_currentSong.value?.id == song.id) {
                    if (!resolvedUrl.isNullOrBlank()) {
                        playUrl(resolvedUrl, song, retryWithItunes = false)
                    } else {
                        // Stop buffering gracefully if no preview URL is available
                        _isBuffering.value = false
                        _durationMs.value = song.durationMs.coerceAtLeast(30000L)
                        _isPlaying.value = false
                        stopProgressTicker()
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        if (player != null) {
            if (_isPlaying.value) {
                player.pause()
                _isPlaying.value = false
                stopProgressTicker()
                startBackgroundService()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressTicker()
                startBackgroundService()
            }
        } else {
            _currentSong.value?.let { playSong(it, currentPlaylist) }
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(30000L))
        _currentPositionMs.value = target
        try {
            mediaPlayer?.seekTo(target.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startBackgroundService()
    }

    fun seekBy(deltaMs: Long) {
        val target = (_currentPositionMs.value + deltaMs).coerceIn(0L, _durationMs.value.coerceAtLeast(30000L))
        seekTo(target)
    }

    fun playNext() {
        if (currentPlaylist.isEmpty()) return
        if (_isShuffle.value && currentPlaylist.size > 1) {
            var nextIndex = (0 until currentPlaylist.size).random()
            if (nextIndex == currentIndexInternal) nextIndex = (nextIndex + 1) % currentPlaylist.size
            currentIndexInternal = nextIndex
        } else {
            currentIndexInternal = (currentIndexInternal + 1) % currentPlaylist.size
        }
        _currentIndex.value = currentIndexInternal
        val nextSong = currentPlaylist.getOrNull(currentIndexInternal) ?: return
        playSong(nextSong, currentPlaylist)
    }

    fun playPrevious() {
        if (currentPlaylist.isEmpty()) return
        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        currentIndexInternal = if (currentIndexInternal - 1 < 0) currentPlaylist.size - 1 else currentIndexInternal - 1
        _currentIndex.value = currentIndexInternal
        val prevSong = currentPlaylist.getOrNull(currentIndexInternal) ?: return
        playSong(prevSong, currentPlaylist)
    }

    fun reorderUpcoming(fromUpcomingIndex: Int, toUpcomingIndex: Int) {
        val startUpcoming = currentIndexInternal + 1
        if (startUpcoming >= currentPlaylist.size) return
        val absFrom = startUpcoming + fromUpcomingIndex
        val absTo = startUpcoming + toUpcomingIndex
        if (absFrom in startUpcoming until currentPlaylist.size && absTo in startUpcoming until currentPlaylist.size) {
            val moved = currentPlaylist.removeAt(absFrom)
            currentPlaylist.add(absTo, moved)
            _currentQueue.value = currentPlaylist.toList()
        }
    }

    fun moveUpcomingUp(upcomingIndex: Int) {
        if (upcomingIndex > 0) {
            reorderUpcoming(upcomingIndex, upcomingIndex - 1)
        }
    }

    fun moveUpcomingDown(upcomingIndex: Int) {
        val upcomingSize = (currentPlaylist.size - (currentIndexInternal + 1)).coerceAtLeast(0)
        if (upcomingIndex < upcomingSize - 1) {
            reorderUpcoming(upcomingIndex, upcomingIndex + 1)
        }
    }

    fun removeUpcoming(upcomingIndex: Int) {
        val absIndex = currentIndexInternal + 1 + upcomingIndex
        if (absIndex in (currentIndexInternal + 1) until currentPlaylist.size) {
            currentPlaylist.removeAt(absIndex)
            _currentQueue.value = currentPlaylist.toList()
        }
    }

    fun removeTrackById(songId: Long) {
        val index = currentPlaylist.indexOfFirst { it.id == songId }
        if (index == -1) return
        if (index == currentIndexInternal) {
            if (currentPlaylist.size > 1) {
                val nextIdx = (currentIndexInternal + 1) % currentPlaylist.size
                val nextSong = currentPlaylist[nextIdx]
                currentPlaylist.removeAt(index)
                currentIndexInternal = currentPlaylist.indexOfFirst { it.id == nextSong.id }.coerceAtLeast(0)
                _currentQueue.value = currentPlaylist.toList()
                _currentIndex.value = currentIndexInternal
                playSong(nextSong, currentPlaylist)
            } else {
                currentPlaylist.clear()
                currentIndexInternal = -1
                _currentQueue.value = emptyList()
                _currentIndex.value = -1
                _currentSong.value = null
                _currentPositionMs.value = 0L
                _isPlaying.value = false
                releaseMediaPlayer()
            }
        } else {
            if (index < currentIndexInternal) {
                currentIndexInternal--
            }
            currentPlaylist.removeAt(index)
            _currentQueue.value = currentPlaylist.toList()
            _currentIndex.value = currentIndexInternal
        }
    }

    fun clearUpcoming() {
        if (currentIndexInternal in currentPlaylist.indices) {
            currentPlaylist = currentPlaylist.subList(0, currentIndexInternal + 1).toMutableList()
            _currentQueue.value = currentPlaylist.toList()
        }
    }

    fun playTrackAtQueueIndex(queueIndex: Int) {
        if (queueIndex in currentPlaylist.indices) {
            currentIndexInternal = queueIndex
            _currentIndex.value = currentIndexInternal
            val song = currentPlaylist[queueIndex]
            playSong(song, currentPlaylist)
        }
    }

    fun addToQueue(song: Song, playNext: Boolean = false) {
        if (currentPlaylist.isEmpty()) {
            playSong(song, listOf(song))
        } else if (playNext) {
            val insertIdx = (currentIndexInternal + 1).coerceAtMost(currentPlaylist.size)
            currentPlaylist.add(insertIdx, song)
            _currentQueue.value = currentPlaylist.toList()
        } else {
            currentPlaylist.add(song)
            _currentQueue.value = currentPlaylist.toList()
        }
    }

    fun addAllToQueue(songs: List<Song>) {
        if (currentPlaylist.isEmpty()) {
            if (songs.isNotEmpty()) {
                playSong(songs.first(), songs)
            }
        } else {
            currentPlaylist.addAll(songs)
            _currentQueue.value = currentPlaylist.toList()
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
        mediaPlayer?.isLooping = _isLooping.value
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    _currentPositionMs.value = pos

                    // Meaningful listening threshold: record listen after 7 seconds or 25% of song duration
                    if (!hasRecordedListen && (pos >= 7000L || pos >= (_durationMs.value * 0.25).toLong())) {
                        hasRecordedListen = true
                        _currentSong.value?.let { current ->
                            onMeaningfulListen?.invoke(current)
                        }
                    }
                } else if (player == null && _isPlaying.value) {
                    val next = _currentPositionMs.value + 200L
                    if (next >= _durationMs.value) {
                        playNext()
                    } else {
                        _currentPositionMs.value = next
                    }
                }
                delay(60L)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releaseMediaPlayer() {
        resolveJob?.cancel()
        resolveJob = null
        watchdogJob?.cancel()
        watchdogJob = null
        stopProgressTicker()
        stopBackgroundService()
        try {
            androidEqualizer?.release()
        } catch (e: Exception) {
            // Ignored
        }
        androidEqualizer = null
        releaseCurrentMediaPlayer()
    }

    private fun startBackgroundService() {
        try {
            MediaPlaybackService.activePlayerManager = this
            val song = _currentSong.value
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.ACTION_UPDATE
                putExtra(MediaPlaybackService.EXTRA_TITLE, song?.title ?: "Musica Player")
                putExtra(MediaPlaybackService.EXTRA_ARTIST, song?.artist ?: "Playing Audio")
                putExtra(MediaPlaybackService.EXTRA_ARTWORK, song?.artworkUrl)
                putExtra(MediaPlaybackService.EXTRA_IS_PLAYING, _isPlaying.value)
                putExtra(MediaPlaybackService.EXTRA_POSITION, _currentPositionMs.value)
                putExtra(MediaPlaybackService.EXTRA_DURATION, _durationMs.value)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopBackgroundService() {
        try {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = MediaPlaybackService.ACTION_STOP
            }
            context.stopService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        releaseMediaPlayer()
    }
}

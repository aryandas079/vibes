package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Status Bar Background Media Playback Service.
 * Posts an ongoing MediaStyle notification in the status bar and notification drawer
 * complete with album artwork, title, artist, playback controls (Previous, Play/Pause, Next),
 * WakeLock for uninterrupted background audio, and MediaSession for lockscreen & status bar.
 */
class MediaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "vibes_media_playback_channel"
        const val NOTIFICATION_ID = 8881

        const val ACTION_START = "com.example.ACTION_START"
        const val ACTION_STOP = "com.example.ACTION_STOP"
        const val ACTION_TOGGLE_PLAY = "com.example.ACTION_TOGGLE_PLAY"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_UPDATE = "com.example.ACTION_UPDATE"

        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_ARTIST = "EXTRA_ARTIST"
        const val EXTRA_ARTWORK = "EXTRA_ARTWORK"
        const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_DURATION = "EXTRA_DURATION"

        const val ACTION_APP_FOREGROUND_CHANGED = "com.example.ACTION_APP_FOREGROUND_CHANGED"
        const val EXTRA_IS_FOREGROUND = "EXTRA_IS_FOREGROUND"

        var activePlayerManager: AudioPlayerManager? = null
        var isServiceRunning = false

        @Volatile
        private var instance: MediaPlaybackService? = null
        private var _isAppInForeground: Boolean = true
        val isAppInForeground: Boolean get() = _isAppInForeground

        fun setAppInForeground(isForeground: Boolean) {
            _isAppInForeground = isForeground
            instance?.handleAppForegroundChanged(isForeground)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaSession: MediaSession? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var floatingPillManager: FloatingPillManager

    private var currentTitle = "Vibes Player"
    private var currentArtist = "Playing Audio"
    private var currentArtworkUrl: String? = null
    private var isPlaying = false
    private var currentPositionMs = 0L
    private var currentDurationMs = 30000L
    private var artworkBitmap: Bitmap? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        isServiceRunning = true
        floatingPillManager = FloatingPillManager(this)

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Vibes:PlaybackService")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        createNotificationChannel()
        initMediaSession()
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "VibesMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    activePlayerManager?.togglePlayPause()
                }

                override fun onPause() {
                    activePlayerManager?.togglePlayPause()
                }

                override fun onSkipToNext() {
                    activePlayerManager?.playNext()
                }

                override fun onSkipToPrevious() {
                    activePlayerManager?.playPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    activePlayerManager?.seekTo(pos)
                }
            })
            isActive = true
        }
    }

    fun handleAppForegroundChanged(isForeground: Boolean) {
        if (isForeground) {
            floatingPillManager.hide()
        } else {
            val hasSong = activePlayerManager?.currentSong?.value != null || currentTitle != "Vibes Player"
            if (hasSong && isPlaying) {
                floatingPillManager.show(
                    title = currentTitle,
                    artist = currentArtist,
                    artworkUrl = currentArtworkUrl,
                    artworkBitmap = artworkBitmap,
                    isPlaying = isPlaying
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_APP_FOREGROUND_CHANGED -> {
                val isForeground = intent.getBooleanExtra(EXTRA_IS_FOREGROUND, true)
                setAppInForeground(isForeground)
            }

            ACTION_START, ACTION_UPDATE -> {
                intent.getStringExtra(EXTRA_TITLE)?.let { if (it.isNotBlank()) currentTitle = it }
                intent.getStringExtra(EXTRA_ARTIST)?.let { if (it.isNotBlank()) currentArtist = it }
                val newArt = intent.getStringExtra(EXTRA_ARTWORK)
                if (newArt != currentArtworkUrl || artworkBitmap == null) {
                    currentArtworkUrl = newArt
                    loadArtworkBitmap(newArt)
                }
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying)
                currentPositionMs = intent.getLongExtra(EXTRA_POSITION, currentPositionMs)
                currentDurationMs = intent.getLongExtra(EXTRA_DURATION, currentDurationMs)

                manageWakeLock(isPlaying)
                updatePlaybackState()
                updateNotification()

                // Update floating pill if app is running in background
                if (!isAppInForeground) {
                    val hasSong = activePlayerManager?.currentSong?.value != null || currentTitle != "Vibes Player"
                    if (hasSong && isPlaying) {
                        floatingPillManager.update(
                            title = currentTitle,
                            artist = currentArtist,
                            artworkUrl = currentArtworkUrl,
                            artworkBitmap = artworkBitmap,
                            isPlaying = isPlaying
                        )
                    } else if (!hasSong) {
                        floatingPillManager.hide()
                    }
                }
            }

            ACTION_TOGGLE_PLAY -> {
                activePlayerManager?.togglePlayPause()
            }

            ACTION_PREVIOUS -> {
                activePlayerManager?.playPrevious()
            }

            ACTION_NEXT -> {
                activePlayerManager?.playNext()
            }

            ACTION_STOP -> {
                floatingPillManager.hide()
                manageWakeLock(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun manageWakeLock(acquire: Boolean) {
        try {
            if (acquire) {
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire(3 * 60 * 60 * 1000L) // 3 hours max
                }
            } else {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePlaybackState() {
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO
            )
            .setState(state, currentPositionMs, 1.0f)
            .build()

        mediaSession?.setPlaybackState(playbackState)

        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, currentTitle)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, currentArtist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, "Vibes Music")
            .putLong(MediaMetadata.METADATA_KEY_DURATION, currentDurationMs)

        artworkBitmap?.let {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, it)
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, it)
        }

        mediaSession?.setMetadata(metadataBuilder.build())
    }

    private fun loadArtworkBitmap(url: String?) {
        if (url.isNullOrBlank()) {
            artworkBitmap = generateDefaultBitmap(currentTitle)
            updateNotification()
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(this@MediaPlaybackService)
                val request = ImageRequest.Builder(this@MediaPlaybackService)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    artworkBitmap = bitmap
                    withContext(Dispatchers.Main) {
                        updatePlaybackState()
                        updateNotification()
                        if (!isAppInForeground && isPlaying) {
                            floatingPillManager.update(
                                title = currentTitle,
                                artist = currentArtist,
                                artworkUrl = currentArtworkUrl,
                                artworkBitmap = artworkBitmap,
                                isPlaying = isPlaying
                            )
                        }
                    }
                } else {
                    artworkBitmap = generateDefaultBitmap(currentTitle)
                    withContext(Dispatchers.Main) {
                        updateNotification()
                    }
                }
            } catch (e: Exception) {
                artworkBitmap = generateDefaultBitmap(currentTitle)
                withContext(Dispatchers.Main) {
                    updateNotification()
                }
            }
        }
    }

    private fun generateDefaultBitmap(title: String): Bitmap {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#1DB954"))
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 96f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val firstChar = title.trim().firstOrNull()?.uppercase() ?: "V"
        val y = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(firstChar, canvas.width / 2f, y, paint)
        return bitmap
    }

    private fun updateNotification() {
        val notification = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playToggleIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val playTogglePendingIntent = PendingIntent.getService(this, 2, playToggleIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSubText("Vibes Hi-Fi")
            .setSmallIcon(R.drawable.ic_vibes_logo)
            .setLargeIcon(artworkBitmap)
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(
                PendingIntent.getService(
                    this,
                    4,
                    Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_STOP },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playIcon, if (isPlaying) "Pause" else "Play", playTogglePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        // Native Android MediaStyle notification (Spotify/Apple Music styled)
        val mediaStyle = MediaNotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)

        mediaSession?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(MediaSessionCompat.Token.fromToken(token))
        }
        builder.setStyle(mediaStyle)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vibes Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background music player and status bar indicator"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
        floatingPillManager.destroy()
        isServiceRunning = false
        manageWakeLock(false)
        wakeLock = null
        mediaSession?.release()
        mediaSession = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

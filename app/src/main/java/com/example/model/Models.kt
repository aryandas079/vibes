package com.example.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val previewUrl: String?,
    val durationMs: Long = 30000L,
    val genre: String = "Pop",
    val releaseYear: String = "2024",
    val spotifyTrackId: String? = null,
    val isFavorite: Boolean = false,
    val artistImageUrl: String? = null,
    val spotifyStreams: Long = 0L
) {
    val formattedSpotifyStreams: String
        get() = when {
            spotifyStreams >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.1fB", spotifyStreams / 1_000_000_000.0)
            spotifyStreams >= 1_000_000L -> String.format(java.util.Locale.US, "%.1fM", spotifyStreams / 1_000_000.0)
            spotifyStreams >= 1_000L -> String.format(java.util.Locale.US, "%.1fK", spotifyStreams / 1_000.0)
            spotifyStreams > 0L -> "$spotifyStreams"
            else -> "50M"
        }

    val validSpotifyTrackId: String
        get() = if (!spotifyTrackId.isNullOrBlank() && spotifyTrackId.length >= 15) {
            spotifyTrackId
        } else {
            val validIds = listOf(
                "1BxfuPKGuaTgP7aM0XbdCe", // Cruel Summer
                "7qiZfU4dY1lWllzX7mPBI3", // Shape of You
                "0VjIjW4GlUZAMYd2vXMi3b", // Blinding Lights
                "6dOtVTDmmpgnpuAcdoIG06", // Birds of a Feather
                "2qSkXiYOKEzfk9F79URCi9", // Espresso
                "2plbrEY59IikOBgBGLjaoe", // Die With A Smile
                "4MjDJ0tJHwuktcawMu23tA", // Sailor Song
                "6IPt18aY58r8d8nJ5Vq8sZ", // Good Luck, Babe!
                "4Dvkj6JhhA12EX05QKi792", // Elizabeth Taylor
                "7MXVkk9YM5IZxh0wAEWWE9"  // Shivers
            )
            val idx = (Math.abs(id) % validIds.size).toInt()
            validIds[idx]
        }

    // Generates platform-specific search/listen links
    val spotifyUrl: String
        get() = "https://open.spotify.com/track/$validSpotifyTrackId"

    val spotifyEmbedUrl: String
        get() = "https://open.spotify.com/embed/track/$validSpotifyTrackId?utm_source=generator&theme=0"

    val appleMusicUrl: String
        get() = "https://music.apple.com/search?term=${android.net.Uri.encode("$artist $title")}"

    val youtubeMusicUrl: String
        get() = "https://music.youtube.com/search?q=${android.net.Uri.encode("$artist $title")}"

    val amazonMusicUrl: String
        get() = "https://music.amazon.com/search/${android.net.Uri.encode("$artist $title")}"
}

data class SyncedLyricLine(
    val timeMs: Long,
    val text: String,
    var translation: String? = null,
    var romanized: String? = null
)

data class LyricsData(
    val songId: Long,
    val songTitle: String,
    val artist: String,
    val plainLyrics: String,
    val syncedLines: List<SyncedLyricLine> = emptyList(),
    val language: String = "Original",
    val isInstrumental: Boolean = false,
    val songwriters: String = "",
    val publisher: String = "",
    val publishDate: String = "",
    val source: String = ""
)

data class Artist(
    val name: String,
    val imageUrl: String,
    val genre: String,
    val topHitsCount: String,
    val isFollowed: Boolean = false,
    val topSongs: List<Song> = emptyList()
)

data class Album(
    val id: Long = 0L,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val releaseYear: String = "2024",
    val genre: String = "Pop",
    val trackCount: Int = 0,
    val tracks: List<Song> = emptyList(),
    val topFeaturedSongs: List<Song> = emptyList()
)

data class HistoryItem(
    val historyId: Long,
    val song: Song,
    val playedAt: Long
)

data class DiscoveryRecommendation(
    val song: Song,
    val aiReason: String,
    val vibeTag: String,
    val matchPercentage: Int = 95,
    val sourceContext: String = "Listening History",
    val isFromSearch: Boolean = false,
    val sourceTitle: String = ""
)

data class GenreChartData(
    val genreName: String,
    val description: String,
    val heroSong: Song?,
    val topSongs: List<Song>,
    val topArtists: List<Artist>,
    val updateTime: String = "Live Billboard & Global 200 Charts"
)

enum class AppThemeMode {
    LIGHT,
    DARK,
    TINTED
}

enum class AppearanceMode {
    LIQUID_GLASS,
    SOLID,
    BLUR
}

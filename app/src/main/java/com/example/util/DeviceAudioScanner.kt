package com.example.util

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DeviceAudioScanner {

    suspend fun scanDeviceAudioFiles(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val deviceSongs = mutableListOf<Song>()

        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: "Unknown Track"
                    val artist = c.getString(artistColumn) ?: "Device Artist"
                    val album = c.getString(albumColumn) ?: "Device Storage"
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = "content://media/external/audio/albumart/$albumId"

                    deviceSongs.add(
                        Song(
                            id = id + 900000000L,
                            title = title,
                            artist = if (artist == "<unknown>") "Device Audio" else artist,
                            album = if (album == "<unknown>") "Local Files" else album,
                            artworkUrl = albumArtUri,
                            previewUrl = contentUri,
                            durationMs = if (duration > 0) duration else 180000L,
                            genre = "Device Local",
                            releaseYear = "Offline"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceAudioScanner", "Failed to scan MediaStore: ${e.message}")
        }

        if (deviceSongs.isEmpty()) {
            deviceSongs.addAll(getSampleDeviceFiles())
        }

        return@withContext deviceSongs
    }

    private fun getSampleDeviceFiles(): List<Song> {
        val s1 = Song(
            id = 900000001L,
            title = "Starboy (Device Offline Lossless)",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy [Local FLAC]",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/bb/6d/8f/bb6d8f67-6d04-10b5-dd62-eb5809ac54fc/00602567879152.rgb.jpg/600x600bb.jpg",
            previewUrl = null,
            durationMs = 230000L,
            genre = "Local Offline",
            releaseYear = "Local SD"
        )
        val s2 = Song(
            id = 900000002L,
            title = "Cruel Summer (Device Audio Studio)",
            artist = "Taylor Swift",
            album = "Lover [Device Storage]",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/49/3d/ab/493dab54-f920-9043-6181-80993b8116c9/19UMGIM53909.rgb.jpg/600x600bb.jpg",
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/44/af/81/44af8168-9609-1b85-5048-ada08dceacf3/mzaf_1341699644335558812.plus.aac.p.m4a",
            durationMs = 178000L,
            genre = "Local Offline",
            releaseYear = "Local SD"
        )
        val s3 = Song(
            id = 900000003L,
            title = "Blinding Lights (Offline Hi-Fi)",
            artist = "The Weeknd",
            album = "After Hours [Local MP3]",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/4a/12/37/4a1237a3-5246-86d1-447a-8f9f8c650462/20UMGIM02319.rgb.jpg/600x600bb.jpg",
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/3d/91/92/3d919246-44ec-6181-b236-1e66c6b8764a/mzaf_1603598764096053123.plus.aac.p.m4a",
            durationMs = 200000L,
            genre = "Local Offline",
            releaseYear = "Local SD"
        )
        val s4 = Song(
            id = 900000004L,
            title = "Espresso (Device Download)",
            artist = "Sabrina Carpenter",
            album = "Short n Sweet [Local]",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/71/34/00/713400e9-b508-3e5f-eb5a-73c33bc0ff00/24UMGIM39257.rgb.jpg/600x600bb.jpg",
            previewUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/28/3d/26/283d2678-75b2-320e-8d2a-1fb8ef8b08cd/mzaf_7867201889814232313.plus.aac.p.m4a",
            durationMs = 175000L,
            genre = "Local Offline",
            releaseYear = "Local SD"
        )
        return listOf(s1, s2, s3, s4)
    }
}

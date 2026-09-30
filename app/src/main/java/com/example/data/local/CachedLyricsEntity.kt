package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lyrics")
data class CachedLyricsEntity(
    @PrimaryKey val songId: Long,
    val songTitle: String,
    val artist: String,
    val plainLyrics: String,
    val songwriters: String = "",
    val publisher: String = "",
    val publishDate: String = "",
    val source: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

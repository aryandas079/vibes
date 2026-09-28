package com.example.model

/**
 * Data models for Gemini-powered mood and activity track sequence playlists.
 */
data class GeminiTrackSequence(
    val sequenceNumber: Int,
    val title: String,
    val artist: String,
    val tempo: String,
    val energyStage: String,
    val transitionReason: String,
    val resolvedSong: Song? = null
)

data class GeminiMoodPlaylist(
    val title: String,
    val mood: String,
    val activity: String,
    val narrativeArc: String,
    val vibeDescription: String,
    val tracks: List<GeminiTrackSequence>
)

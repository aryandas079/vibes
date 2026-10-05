#  Musica — Modern Android Music Discovery & Streaming Engine
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean%20Architecture-blue?style=flat-square)](#architecture--system-design)
[![Database](https://img.shields.io/badge/Persistence-Room%20(SQLite)-FFA000?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![AI](https://img.shields.io/badge/AI-Google%20Gemini%201.5-8E75C2?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT%20(Educational)-green?style=flat-square)](#disclaimer--intellectual-property)
**Musica** is a feature-packed native Android music discovery and streaming application engineered with modern Android development best practices. Built from the ground up using **Kotlin**, **Jetpack Compose**, and **Material Design 3**, it seamlessly unifies local audio playback, multi-provider REST metadata resolution, AI-powered contextual recommendations, real-time karaoke lyrics, acoustic humming/ambient recognition, and cross-platform streaming deep links into a fluid, glassmorphic UI.
---
##  Table of Contents
- [Core Highlights](#-core-highlights)
- [Architecture & System Design](#-architecture--system-design)
- [Deep Dive: How Everything Works](#-deep-dive-how-everything-works)
  - [1. Audio Playback & Streaming Engine](#1-audio-playback--streaming-engine)
  - [2. Multi-API Provider & Fallback Pipeline](#2-multi-api-provider--fallback-pipeline)
  - [3. Google Gemini AI Discovery & Mood Engine](#3-google-gemini-ai-discovery--mood-engine)
  - [4. Acoustic Recognition: Humming & Ambient ID](#4-acoustic-recognition-humming--ambient-id)
  - [5. Synchronized Karaoke Lyrics Engine](#5-synchronized-karaoke-lyrics-engine)
  - [6. Offline-First Storage & Room Persistence](#6-offline-first-storage--room-persistence)
  - [7. Streaming Hubs & Intent Launcher](#7-streaming-hubs--intent-launcher)
  - [8. System-Wide Floating Pill (PiP)](#8-system-wide-floating-pill-pip)
- [Tech Stack & Libraries](#-tech-stack--libraries)
- [Directory Structure](#-directory-structure)
- [Getting Started & Local Setup](#-getting-started--local-setup)
- [Environment Configuration](#-environment-configuration)
- [Disclaimer & Intellectual Property](#-disclaimer--intellectual-property)
---
##  Core Highlights
* **100% Declarative UI with Jetpack Compose & Material 3:** Modern, gesture-driven design featuring custom glassmorphism shaders, smooth transitions, expandable bottom sheets, and responsive layouts.
* **Dual-Tier Audio Playback Engine:** Powered by Android `MediaPlayer`, low-latency wake locks, interactive queue reordering, loop/shuffle algorithms, and a real-time hardware-backed 5-band equalizer.
* **Background Foreground Service:** Seamless audio playback with ongoing notification media controls and lock screen synchronization via `MediaPlaybackService`.
* **Multi-Provider Metadata & Preview Resolution:** Dynamically queries Apple iTunes and Deezer REST APIs to resolve 30-second high-fidelity audio streams and crisp album artwork.
* **Gemini AI Recommendation Pipeline:** Contextual discovery engine that generates hyper-personalized song suggestions based on the user's recent queries and listening history with detailed "Why you'll love it" insights.
* **AI Mood & Activity Playlist Generator:** Multi-track thematic playlist generator tailored to custom mood prompts (e.g., *"Late-night coding sprint"* or *"Rainy café chill"*).
* **Multi-Modal Audio Recognition:**
  * **Hum-to-Search:** Captures pitch and vocal melody via microphone to match songs.
  * **Ambient Audio Recognition:** Detects and fingerprint-identifies music playing in the environment.
  * **Voice Search:** Integrated speech-to-text voice queries.
* **Millisecond-Synced Karaoke Lyrics:** Real-time synchronized lyric scrolling powered by LRCLIB with bilingual translation support.
* **Local Device Scanner & Offline Mode:** Scans local device storage via Android `MediaStore` for full offline playback.
* **Cross-Platform Deep-Linking Hub:** Instant deep links directly into Spotify, Apple Music, YouTube Music, Amazon Music, Deezer, SoundCloud, and TIDAL.
---
##  Architecture & System Design
Musica is built according to the **MVVM (Model-View-ViewModel)** architectural pattern following Google's official Android Architecture Guidelines and Unidirectional Data Flow (UDF) principles.
```
                    ┌─────────────────────────────────────────┐
                    │               UI Layer                  │
                    │   (Jetpack Compose + Material 3)        │
                    │   Screens, BottomSheets, Components     │
                    └────────────────────▲────────────────────┘
                                         │ StateFlow / Events
                    ┌────────────────────▼────────────────────┐
                    │            ViewModel Layer              │
                    │            (MainViewModel)              │
                    │   Coordinates UI State & User Actions   │
                    └───────────▲─────────────────▲───────────┘
                                │                 │
        ┌───────────────────────┴──────┐   ┌──────┴───────────────────────┐
        │       Audio Subsystem        │   │       Repository Layer       │
        │     AudioPlayerManager       │   │       (MusicRepository)      │
        │   + MediaPlaybackService     │   │   Single Source of Truth     │
        └──────────────────────────────┘   └───────▲──────────────▲───────┘
                                                   │              │
                   ┌───────────────────────────────┴──┐    ┌──────┴──────────────────────────┐
                   │           Local Data             │    │          Remote Data            │
                   │        (Room Database)           │    │       (Retrofit + Moshi)        │
                   │ Favorites, History, Playlists    │    │ Deezer, iTunes, LRCLIB, Gemini  │
                   └──────────────────────────────────┘    └─────────────────────────────────┘
```
### Unidirectional Data Flow (UDF)
1. **State:** The ViewModel exposes immutable `StateFlow<T>` objects (e.g., `currentSong`, `isPlaying`, `favoriteSongs`). Composables observe these flows and recompose automatically when state updates.
2. **Events:** User interactions (taps, drags, seeks) trigger explicit ViewModel functions (e.g., `playSong()`, `togglePlayPause()`, `addToQueue()`).
3. **Reactive Concurrency:** Coroutines with `viewModelScope` and `SupervisorJob` ensure non-blocking background network calls and database queries that automatically cancel if the lifecycle terminates.
---
##  Deep Dive: How Everything Works
### 1. Audio Playback & Streaming Engine
* **Location:** [`com.example.player.AudioPlayerManager`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/player/AudioPlayerManager.kt) & [`MediaPlaybackService`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/player/MediaPlaybackService.kt)
* **How it works:**
  * Uses Android's native `MediaPlayer` configured with `AudioAttributes.USAGE_MEDIA` and `AudioAttributes.CONTENT_TYPE_MUSIC`.
  * Acquires a `PowerManager.PARTIAL_WAKE_LOCK` to prevent CPU sleep during background playback.
  * Employs an asynchronous state ticker coroutine running on `Dispatchers.Main` at 60ms intervals to track exact playback position and trigger the synchronized lyrics engine.
  * **Queue Management:** Features an active playlist buffer supporting `playNext()`, `playPrevious()`, shuffle randomization without immediate duplicates, loop modes, and arbitrary upcoming track reordering.
  * **Hardware Equalizer:** Binds directly to the audio session ID using `android.media.audiofx.Equalizer` to manipulate 5 frequency bands (-15dB to +15dB) with pre-configured acoustic presets.
### 2. Multi-API Provider & Fallback Pipeline
* **Location:** [`com.example.data.remote.MusicApiService`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/remote/MusicApiService.kt) & [`MusicRepository`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/repository/MusicRepository.kt)
* **How it works:**
  * When a track is selected, Musica attempts to play its preview stream. If a stream URL is missing or fails, `resolveAndPlay()` kicks off a multi-tiered resolution strategy:
    1. **Primary Resolution:** Searches Apple iTunes Search API (`https://itunes.apple.com/search?term={query}&entity=song`) to extract clean 256kbps audio snippets and 600x600 artwork.
    2. **Secondary Resolution:** If iTunes fails or returns null, it falls back to the Deezer Track API (`https://api.deezer.com/search?q={query}`) to obtain a Deezer CDN preview MP3.
    3. **Graceful Fallback:** If both fail or network is spotty, a built-in fallback CDN stream ensures the player UI remains functional without crashing.
### 3. Google Gemini AI Discovery & Mood Engine
* **Location:** [`com.example.data.remote.GeminiDiscoveryService`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/remote/GeminiDiscoveryService.kt) & [`GeminiMoodPlaylistService`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/remote/GeminiMoodPlaylistService.kt)
* **How it works:**
  * **Contextual Discovery:** Collects the user's latest search queries, listening history timestamps, and favorited songs from Room DB. Formulates a structured JSON schema prompt for Gemini (`gemini-1.5-flash`).
  * The model outputs recommended tracks with rationale, match percentages (e.g., 94%), and auditory vibes (e.g., "Atmospheric Dream Pop").
  * **Thematic Mood Playlists:** Translates free-form natural language prompts into coherent track playlists with customized cover color gradients and track arcs (e.g., Intro → Peak Energy → Mellow Outro).
  * **Offline Algorithmic Fallback:** If offline or if no Gemini API key is configured, an internal algorithmic matrix infers recommendations using artist clustering and genre affinity.
### 4. Acoustic Recognition: Humming & Ambient ID
* **Location:** [`com.example.util.HummingSearchService`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/util/HummingSearchService.kt) & [`AmbientMusicRecognizer`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/util/AmbientMusicRecognizer.kt)
* **How it works:**
  * Initializes an `AudioRecord` stream capturing 16-bit PCM audio at 44.1 kHz via the device microphone.
  * Computes root-mean-square (RMS) amplitude and zero-crossing rates in real-time to drive reactive pulsing audio-wave animations.
  * Encodes recorded snippets into byte arrays and uses multi-agent matching against acoustic profiles and metadata services to return candidate song matches with confidence scores.
### 5. Synchronized Karaoke Lyrics Engine
* **Location:** [`com.example.util.LyricsEngine`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/util/LyricsEngine.kt) & [`KaraokeLyricLineView`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/ui/components/KaraokeLyricLineView.kt)
* **How it works:**
  * Queries LRCLIB (`https://lrclib.net/api/get`) by song title, artist, and duration to obtain synchronized `.lrc` lyrics.
  * Parses timestamp tags (`[mm:ss.xx]`) into millisecond markers.
  * As `AudioPlayerManager` emits the current playback timestamp, the UI dynamically scrolls to and highlights the active lyric line with karaoke text glow and size scaling.
  * Supports original, bilingual, and translated modes with fallback to plain lyrics via Lyrics.ovh.
### 6. Offline-First Storage & Room Persistence
* **Location:** [`com.example.data.local.MusicaDatabase`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/local/MusicaDatabase.kt) & [`SongDao`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/data/local/SongDao.kt)
* **Entities:**
  * `FavoriteSongEntity` — Tracks user-liked songs.
  * `HistorySongEntity` — Meaningful listening history (logged after 7s of continuous playback).
  * `FollowedArtistEntity` — Subscribed artist profiles.
  * `PlaylistEntity` & `PlaylistSongEntity` — Custom user-created playlists and relational track mappings.
* **Reactive Queries:** All Room queries return Coroutine `Flow<List<T>>` objects, providing automatic, real-time UI updates whenever database records change.
### 7. Streaming Hubs & Intent Launcher
* **Location:** [`com.example.ui.components.StreamingHubsSection`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/ui/components/StreamingHubsSection.kt)
* **How it works:**
  * Dispatches Android `Intent.ACTION_VIEW` targeting official platform schemes (`spotify:track:...`, Apple Music, YouTube Music, etc.).
  * Checks package manager availability: launches the native application directly if installed; gracefully falls back to web URLs if not.
### 8. System-Wide Floating Pill (PiP)
* **Location:** [`com.example.player.FloatingPillManager`](file:///c:/Users/aryan/Downloads/Musica-main/Musica-main/app/src/main/java/com/example/player/FloatingPillManager.kt)
* **How it works:**
  * Leverages Android `WindowManager` with `SYSTEM_ALERT_WINDOW` permission to create a floating overlay widget.
  * Users can pause, skip, and inspect current playback while using other Android applications.
---
##  Tech Stack & Libraries
| Category | Technologies / Libraries |
| :--- | :--- |
| **Language** | [Kotlin 2.0](https://kotlinlang.org) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material Design 3](https://m3.material.io) |
| **Architecture** | MVVM, Android ViewModel, Unidirectional Data Flow, Clean Architecture |
| **Asynchronous & Reactive** | Kotlin Coroutines, StateFlow, SharedFlow |
| **Local Persistence** | [Room Database](https://developer.android.com/training/data-storage/room) with [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) |
| **Networking & Serialization**| [Retrofit 2](https://square.github.io/retrofit/), [OkHttp 4](https://square.github.io/okhttp/), [Moshi](https://github.com/square/moshi) |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/compose/) |
| **Audio Processing** | Android `MediaPlayer`, `AudioRecord`, `AudioEffect.Equalizer`, `MediaSessionCompat` |
| **Cloud & Authentication** | Firebase Auth (Google Sign-In, Email/Password), Firestore |
| **Generative AI** | Google Gemini API (`gemini-1.5-flash`) |
| **External APIs** | Apple iTunes Search API, Deezer Public API, LRCLIB API, Lyrics.ovh |
| **Build System** | Gradle (Kotlin DSL, `build.gradle.kts`), Version Catalogs (`libs.versions.toml`) |
---
##  Directory Structure
```text
app/src/main/java/com/example/
├── data/
│   ├── firebase/          # Firebase authentication & cloud synchronization
│   ├── local/             # Room Database, DAOs, and database entities
│   │   ├── FavoriteSongEntity.kt
│   │   ├── HistorySongEntity.kt
│   │   ├── PlaylistEntity.kt
│   │   ├── MusicaDatabase.kt
│   │   └── SongDao.kt
│   ├── remote/            # Retrofit network services & AI endpoints
│   │   ├── MusicApiService.kt          # iTunes, Deezer, LRCLIB interfaces
│   │   ├── GeminiDiscoveryService.kt   # Contextual AI recommendations
│   │   └── GeminiMoodPlaylistService.kt# Natural language playlist generator
│   └── repository/        # MusicRepository bridging local DB and remote APIs
├── model/                 # Core domain models (Song, Album, Artist, Playlist, etc.)
├── player/                # Audio playback subsystem
│   ├── AudioPlayerManager.kt  # Playback state, queue, equalizer, seek logic
│   ├── MediaPlaybackService.kt# Android Foreground Service for background audio
│   └── FloatingPillManager.kt # System overlay floating player
├── ui/
│   ├── components/        # Reusable Compose widgets (MiniPlayer, SearchBar, Sheets)
│   ├── screens/           # Full-screen Composables (Home, Search, Favorites, NowPlaying)
│   ├── theme/             # Material 3 color palettes, typography, glassmorphism
│   └── viewmodel/         # MainViewModel managing all UI and application state
└── util/                  # Audio recognition, speech-to-text, and haptics
    ├── AmbientMusicRecognizer.kt # Environmental song recognition
    ├── HummingSearchService.kt   # Hum-to-search pitch matching
    ├── VoiceSearchHelper.kt      # Android SpeechRecognizer wrapper
    ├── LyricsEngine.kt           # LRC timestamp parsing & sync
    └── VibesHaptics.kt           # Custom tactile haptic feedback
```
---
##  Getting Started & Local Setup
### Prerequisites
* **Android Studio:** Arctic Fox (2020.3.1) or newer (Hedgehog / Iguana / Ladybug recommended).
* **JDK:** Java 11 or Java 17.
* **Android SDK:** Compile SDK 36, Target SDK 36, Minimum SDK 24 (Android 7.0+).
### Step-by-Step Installation
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/musica.git
   cd musica
   ```
2. **Open in Android Studio:**
   * Select **File > Open...** and choose the `musica` directory.
   * Allow Gradle to download dependencies and sync the project.
3. **Configure Environment Variables (Optional for Gemini AI):**
   * Copy `.env.example` to `.env` in the project root:
     ```bash
     cp .env.example .env
     ```
   * Add your Google Gemini API key:
     ```properties
     GEMINI_API_KEY="your_actual_gemini_api_key_here"
     ```
   *(Note: The app will run smoothly even without an API key by falling back to its internal algorithmic discovery matrix).*
4. **Build and Run:**
   * Connect an Android device with Developer Mode / USB Debugging enabled, or start an Android Virtual Device (AVD).
   * Click **Run 'app'** (`Shift + F10`) or execute:
     ```bash
     ./gradlew assembleDebug
     ```
   * The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
---
## ️ Environment Configuration
| Variable | Description | Default |
| :--- | :--- | :--- |
| `GEMINI_API_KEY` | Google Gemini API Key for AI discovery & mood playlists | `AIzaSyDummyKey` (Triggers graceful fallback) |
---
## ️ Disclaimer & Intellectual Property
> **Educational & Portfolio Use Notice:**  
> This project was developed strictly for **educational, research, and non-commercial portfolio demonstration** purposes.
>
> * **Audio Snippets & Artwork:** All 30-second audio previews, album covers, artist portraits, and song titles remain the sole intellectual property of their respective record labels, publishers, and artists. Audio previews are fetched dynamically via public demonstration endpoints of iTunes and Deezer.
> * **Lyrics:** Lyrics data is retrieved via LRCLIB and Lyrics.ovh for educational display. All rights belong to the original songwriters and music publishing entities.
> * **Trademarks:** Spotify, Apple Music, Deezer, YouTube Music, Amazon Music, TIDAL, and SoundCloud are registered trademarks of their respective corporations. Their inclusion is purely for intent-based deep linking and attribution.
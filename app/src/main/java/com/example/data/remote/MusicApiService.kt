package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ItunesResponse(
    val resultCount: Int? = 0,
    val results: List<ItunesTrackItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ItunesTrackItem(
    val trackId: Long? = null,
    val collectionId: Long? = null,
    val wrapperType: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val artworkUrl60: String? = null,
    val previewUrl: String? = null,
    val trackTimeMillis: Long? = null,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null,
    val trackCount: Int? = null,
    val trackNumber: Int? = null
)

@JsonClass(generateAdapter = true)
data class LrclibResponse(
    val id: Long? = null,
    val name: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val instrumental: Boolean? = false,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

@JsonClass(generateAdapter = true)
data class DeezerArtistSearchResponse(
    val data: List<DeezerArtistItem> = emptyList(),
    val total: Int? = 0
)

@JsonClass(generateAdapter = true)
data class DeezerTrackSearchResponse(
    val data: List<DeezerTrackItem> = emptyList(),
    val total: Int? = 0
)

@JsonClass(generateAdapter = true)
data class DeezerTrackItem(
    val id: Long? = null,
    val title: String? = null,
    val preview: String? = null,
    val duration: Long? = null,
    val artist: DeezerArtistShort? = null,
    val album: DeezerAlbumShort? = null,
    val rank: Long? = null
)

@JsonClass(generateAdapter = true)
data class DeezerArtistShort(
    val id: Long? = null,
    val name: String? = null,
    val picture: String? = null,
    val picture_small: String? = null,
    val picture_medium: String? = null,
    val picture_big: String? = null,
    val picture_xl: String? = null
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumShort(
    val id: Long? = null,
    val title: String? = null,
    val cover: String? = null,
    val cover_small: String? = null,
    val cover_medium: String? = null,
    val cover_big: String? = null,
    val cover_xl: String? = null
)

@JsonClass(generateAdapter = true)
data class DeezerArtistItem(
    val id: Long? = null,
    val name: String? = null,
    val picture: String? = null,
    val picture_small: String? = null,
    val picture_medium: String? = null,
    val picture_big: String? = null,
    val picture_xl: String? = null,
    val nb_album: Int? = null,
    val nb_fan: Int? = null
)

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30
    ): DeezerTrackSearchResponse

    @GET("chart/0/tracks")
    suspend fun getGlobalChartTracks(
        @Query("limit") limit: Int = 50
    ): DeezerTrackSearchResponse

    @GET("chart/{genre_id}/tracks")
    suspend fun getGenreChartTracks(
        @Path("genre_id") genreId: Long,
        @Query("limit") limit: Int = 50
    ): DeezerTrackSearchResponse

    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5
    ): DeezerArtistSearchResponse

    @GET("artist/{id}")
    suspend fun getArtist(
        @Path("id") id: Long
    ): DeezerArtistItem

    @GET("artist/{id}/top")
    suspend fun getArtistTopTracks(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 50
    ): DeezerTrackSearchResponse

    @GET("album/{id}/tracks")
    suspend fun getAlbumTracks(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 100
    ): DeezerTrackSearchResponse
}

interface ItunesApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("country") country: String = "US",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 30
    ): ItunesResponse

    @GET("search")
    suspend fun searchAlbums(
        @Query("term") term: String,
        @Query("country") country: String = "US",
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 10
    ): ItunesResponse

    @GET("lookup")
    suspend fun lookupAlbumTracks(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 100
    ): ItunesResponse
}

interface LrclibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null
    ): LrclibResponse

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrclibResponse>
}

@JsonClass(generateAdapter = true)
data class LyricsOvhResponse(
    val lyrics: String? = null,
    val error: String? = null
)

interface LyricsOvhApi {
    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): LyricsOvhResponse
}

@JsonClass(generateAdapter = true)
data class LyristResponse(
    val lyrics: String? = null,
    val title: String? = null,
    val artist: String? = null
)

interface LyristApi {
    @GET("api/{title}/{artist}")
    suspend fun getLyrics(
        @Path("title") title: String,
        @Path("artist") artist: String
    ): LyristResponse
}

object NetworkClient {
    val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MusicaMusicPlayer/2.0 (Android; en-US)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val itunesApi: ItunesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    val deezerApi: DeezerApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DeezerApi::class.java)
    }

    val lrclibApi: LrclibApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LrclibApi::class.java)
    }

    val lyricsOvhApi: LyricsOvhApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.lyrics.ovh/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LyricsOvhApi::class.java)
    }

    val lyristApi: LyristApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://lyrist.vercel.app/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LyristApi::class.java)
    }
}

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteSongEntity::class,
        HistorySongEntity::class,
        CachedSongEntity::class,
        CachedLyricsEntity::class,
        FollowedArtistEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MusicaDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: MusicaDatabase? = null

        fun getInstance(context: Context): MusicaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicaDatabase::class.java,
                    "musica_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

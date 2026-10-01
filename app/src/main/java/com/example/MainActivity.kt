package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MusicaApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.remote.NetworkClient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

import com.example.player.MediaPlaybackService
import android.provider.Settings
import android.content.Intent
import android.net.Uri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS on Android 13+ for status bar media player
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Check overlay permission for floating status bar pill
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Initialize global Coil ImageLoader with OkHttpClient and Caching
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(NetworkClient.okHttpClient)
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            val viewModel: MainViewModel = viewModel()
            MyApplicationTheme {
                MusicaApp(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MediaPlaybackService.setAppInForeground(true)
    }

    override fun onResume() {
        super.onResume()
        MediaPlaybackService.setAppInForeground(true)
    }

    override fun onStop() {
        super.onStop()
        MediaPlaybackService.setAppInForeground(false)
    }
}

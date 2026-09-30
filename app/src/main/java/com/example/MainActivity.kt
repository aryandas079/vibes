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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
}

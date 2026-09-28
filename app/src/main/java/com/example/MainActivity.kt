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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize global Coil ImageLoader to automatically handle crossfades and broken links
        val imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
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

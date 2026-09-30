package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.StormBlackBg
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val badge: String,
    val headline: String,
    val subheadline: String,
    val description: String,
    val features: List<Pair<ImageVector, String>>
)

/**
 * Three-page swipeable onboarding walkthrough for new users.
 * Explains that Vibes is an intelligent music discovery app and presents
 * "Continue with Google" or "Use as a guest" on the final page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onContinueWithGoogle: (Activity) -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val pages = listOf(
        OnboardingPageData(
            badge = "MUSIC DISCOVERY REIMAGINED",
            headline = "Discover Music\nThat Vibes With You",
            subheadline = "Intelligent recommendations tuned to your taste",
            description = "Vibes is built from the ground up for pure music discovery. Uncover hidden gems across diverse genres, explore dynamic charts, and stream high-fidelity audio tailored to your personal mood.",
            features = listOf(
                Icons.Default.Headphones to "Intelligent Genre & Vibe Curation",
                Icons.Default.AutoAwesome to "Daily Personalized Discovery Mixes",
                Icons.Default.Tune to "Lossless Audio Engine & 10-Band EQ"
            )
        ),
        OnboardingPageData(
            badge = "SMART RECOGNITION & LYRICS",
            headline = "Hum, Search &\nIdentify Instantly",
            subheadline = "Never lose a great song again",
            description = "Stuck with a catchy melody in your head? Hum or sing to identify tracks in seconds. Sing along with real-time synchronized scrolling lyrics that match the exact beat of every song.",
            features = listOf(
                Icons.Default.Mic to "Hum & Melody Audio Recognition",
                Icons.Default.GraphicEq to "Beat-Synchronized Real-Time Lyrics",
                Icons.Default.AutoAwesome to "Gemini AI Contextual Recommendations"
            )
        ),
        OnboardingPageData(
            badge = "YOUR AUDIO SANCTUARY",
            headline = "High-Fidelity Audio,\nOnline or Offline",
            subheadline = "Seamless streaming anywhere you go",
            description = "Enjoy uninterrupted gapless listening, download tracks to your offline cache for plane trips and remote areas, and back up your listening history and favorites to Google Cloud.",
            features = listOf(
                Icons.Default.OfflinePin to "Offline Device Storage & Playback",
                Icons.Default.CloudDone to "Firebase Cloud Library Synchronization",
                Icons.Default.Headphones to "Universal Gapless Player with Crossfade"
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StormBlackBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Brand & Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_vibes_logo),
                        contentDescription = "Vibes Logo",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "vibes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke,
                        letterSpacing = 1.sp
                    )
                }

                // Skip button (visible on pages 0 and 1)
                AnimatedVisibility(
                    visible = pagerState.currentPage < 2,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = WhiteSmokeMuted
                        )
                    }
                }
            }

            // Swipeable Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val pageData = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hero Graphic / Illustration
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        WhiteSmokeSoft.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(1.dp, StormSlateBorder, CircleShape)
                            .liquidGlassEffect(shape = CircleShape, elevation = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (pageIndex) {
                            0 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_vibes_logo),
                                    contentDescription = "Vibes",
                                    modifier = Modifier.size(76.dp)
                                )
                            }
                            1 -> {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Audio Recognition",
                                    tint = SpotifyGreen,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                            2 -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Library",
                                    tint = WhiteSmoke,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Badge Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(StormBlackElevated)
                                .border(1.dp, StormSlateBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = pageData.badge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pageIndex == 1) SpotifyGreen else WhiteSmokeSoft,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = pageData.headline,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WhiteSmoke,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = pageData.description,
                            fontSize = 13.sp,
                            color = WhiteSmokeMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 3 Feature Checklist Pills
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pageData.features.forEach { (icon, text) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(StormBlackCard.copy(alpha = 0.6f))
                                        .border(1.dp, StormSlateBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (pageIndex == 1) SpotifyGreen else WhiteSmoke,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WhiteSmokeSoft
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Bottom Navigation & Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isSelected) WhiteSmoke else WhiteSmokeMuted.copy(alpha = 0.35f)
                                )
                        )
                    }
                }

                // If on final page (Page 2): Show "Continue with Google" & "Use as a guest"
                if (pagerState.currentPage == 2) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "Continue with Google" Button
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    onContinueWithGoogle(activity)
                                } else {
                                    onContinueAsGuest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhiteSmoke,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // "Use as a guest" Button
                        OutlinedButton(
                            onClick = onContinueAsGuest,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = WhiteSmoke
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    listOf(WhiteSmokeSoft.copy(alpha = 0.4f), StormSlateBorder)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Use as a guest",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WhiteSmoke
                            )
                        }
                    }
                } else {
                    // "Next" Button on pages 0 and 1
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhiteSmoke,
                            contentColor = StormBlackBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

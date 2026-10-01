package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import coil.compose.AsyncImage
import com.example.model.SyncStatus
import com.example.model.UserSession
import com.example.model.Song
import com.example.ui.components.MusicaImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.util.VibesHaptics
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect
import java.util.Calendar

@Composable
fun HomeAuthHeader(
    userSession: UserSession?,
    syncStatus: SyncStatus,
    onOpenAuth: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    modifier: Modifier = Modifier,
    isOfflineMode: Boolean = false,
    onToggleOfflineMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme
    var showDevInfoDialog by remember { mutableStateOf(false) }

    // Dynamic greeting based on current time
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingTime = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }

    val greetingText = if (userSession != null && !userSession.greetingName.isNullOrBlank() && userSession.greetingName != "Music Lover") {
        "$greetingTime, ${userSession.greetingName}"
    } else {
        greetingTime
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Vibes Logo & Greeting below it
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Vibes Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_vibes_logo),
                        contentDescription = "Vibes Logo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "vibes",
                        fontSize = 13.sp,
                        color = WhiteSmoke,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Greeting placed below Vibes logo (e.g. Good morning, Good afternoon, Good night)
                Text(
                    text = greetingText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))

                // Cloud Sync / Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenAuth()
                    }
                ) {
                    if (userSession != null) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7DE68D))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (syncStatus == SyncStatus.SYNCING) "Firestore Syncing..." else "Cloud Synced • Firebase",
                            fontSize = 11.sp,
                            color = Color(0xFF7DE68D),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Stream • Synced Lyrics • Tap to Sign In",
                            fontSize = 11.sp,
                            color = WhiteSmokeSoft,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Developer Info 'i' Button + Profile Avatar or Sign In button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Small 'i' Info Button (in size of the user profile button)
                val profileButtonSize = if (userSession != null) 42.dp else 38.dp
                Box(
                    modifier = Modifier
                        .size(profileButtonSize)
                        .clip(CircleShape)
                        .background(StormBlackElevated)
                        .border(1.2.dp, StormSlateBorder, CircleShape)
                        .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                        .clickable {
                            VibesHaptics.strongClick(context, view)
                            showDevInfoDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "App Information",
                        tint = WhiteSmoke,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Profile Avatar / Sign In Pill
                if (userSession != null) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, WhiteSmokeSoft, CircleShape)
                            .liquidGlassEffect(shape = CircleShape, elevation = 4.dp)
                            .clickable {
                                VibesHaptics.strongClick(context, view)
                                onOpenAuth()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userSession.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userSession.photoUrl,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = userSession.avatarLetter,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteSmoke
                            )
                        }

                        // Green Cloud Synced indicator badge
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF7DE68D))
                                .border(1.5.dp, StormBlackCard, CircleShape)
                        )
                    }
                } else {
                    // Sign In Button with Google "G" icon
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(StormBlackElevated)
                            .border(1.dp, StormSlateBorder, RoundedCornerShape(19.dp))
                            .clickable {
                                VibesHaptics.strongClick(context, view)
                                onOpenAuth()
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4285F4),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sign In",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WhiteSmoke,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

    }

    if (showDevInfoDialog) {
        AppDevelopmentDialog(
            onDismiss = { showDevInfoDialog = false }
        )
    }
}

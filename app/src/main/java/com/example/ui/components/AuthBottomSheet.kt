package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SyncStatus
import com.example.model.UserSession
import com.example.ui.theme.StormBlackCard
import com.example.ui.theme.StormBlackElevated
import com.example.ui.theme.StormBlackSurface
import com.example.ui.theme.StormSlateBorder
import com.example.ui.theme.WhiteSmoke
import com.example.ui.theme.WhiteSmokeMuted
import com.example.ui.theme.WhiteSmokeSoft
import com.example.ui.theme.liquidGlassEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    sheetState: SheetState,
    userSession: UserSession?,
    syncStatus: SyncStatus,
    lastSyncTime: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignInWithGoogle: (Activity) -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    favoritesCount: Int = 0,
    historyCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StormBlackSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WhiteSmokeMuted.copy(alpha = 0.4f))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (userSession != null) "Account & Cloud Sync" else "Sign In to vibes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteSmoke
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = WhiteSmokeMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error display if any
            if (!errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF331414))
                        .border(1.dp, Color(0xFF882222), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = Color(0xFFFFB4AB)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (userSession != null) {
                // SIGNED IN VIEW
                SignedInContent(
                    userSession = userSession,
                    syncStatus = syncStatus,
                    lastSyncTime = lastSyncTime,
                    favoritesCount = favoritesCount,
                    historyCount = historyCount,
                    onSyncNow = onSyncNow,
                    onSignOut = onSignOut
                )
            } else {
                // SIGNED OUT VIEW - Keep only Continue with Google
                SignedOutContent(
                    isLoading = isLoading,
                    onSignInWithGoogle = {
                        val activity = context as? Activity
                        if (activity != null) {
                            onSignInWithGoogle(activity)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SignedInContent(
    userSession: UserSession,
    syncStatus: SyncStatus,
    lastSyncTime: String?,
    favoritesCount: Int,
    historyCount: Int,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit
) {
    // User Profile Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassEffect(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture or Avatar
            if (!userSession.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = userSession.photoUrl,
                    contentDescription = userSession.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, WhiteSmokeSoft, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF2C3246), Color(0xFF141724))))
                        .border(1.5.dp, WhiteSmokeSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userSession.avatarLetter,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userSession.displayName ?: "Music Fan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteSmoke
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF223522))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CONNECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DE68D)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userSession.email ?: "Signed in via Google",
                    fontSize = 12.sp,
                    color = WhiteSmokeMuted
                )
                Text(
                    text = "Auth: ${userSession.authProvider}",
                    fontSize = 11.sp,
                    color = WhiteSmokeSoft
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Firestore Sync Card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StormBlackCard)
            .border(1.dp, StormSlateBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = "Cloud Sync",
                        tint = if (syncStatus == SyncStatus.SYNCING) WhiteSmokeSoft else Color(0xFF7DE68D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Firebase Firestore Persistence",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WhiteSmoke
                    )
                }

                if (syncStatus == SyncStatus.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = WhiteSmoke,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keeps your favorites ($favoritesCount) and listening history ($historyCount) backed up securely in Google Cloud Firestore.",
                fontSize = 12.sp,
                color = WhiteSmokeMuted,
                lineHeight = 16.sp
            )

            if (lastSyncTime != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastSyncTime,
                    fontSize = 11.sp,
                    color = Color(0xFF7DE68D)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSyncNow,
                enabled = syncStatus != SyncStatus.SYNCING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StormBlackElevated,
                    contentColor = WhiteSmoke
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Now",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (syncStatus == SyncStatus.SYNCING) "Syncing with Firestore..." else "Sync Cloud Library Now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Sign Out Button
    OutlinedButton(
        onClick = onSignOut,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = WhiteSmokeMuted
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(StormSlateBorder, StormSlateBorder))
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Sign Out",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Sign Out",
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SignedOutContent(
    isLoading: Boolean,
    onSignInWithGoogle: () -> Unit
) {
    Column {
        Text(
            text = "Sign in to synchronize your favorite tracks, save your complete listening session history, and back up your library to Google Cloud Firestore.",
            fontSize = 13.sp,
            color = WhiteSmokeMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Google Sign-In Official-styled Button - ONLY sign-in method
        Button(
            onClick = onSignInWithGoogle,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = WhiteSmoke,
                contentColor = Color(0xFF1F1F1F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF1F1F1F),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Signing in with Google...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    // Google "G" letter stylized
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
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Secured with Google Cloud Identity and Firebase Authentication.",
            fontSize = 11.5.sp,
            color = WhiteSmokeSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

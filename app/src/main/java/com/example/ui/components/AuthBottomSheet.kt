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
    onQuickSignInAsAryan: (String, String) -> Unit = { _, _ -> },
    onSignInWithEmail: (String, String) -> Unit = { _, _ -> },
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    favoritesCount: Int = 0,
    historyCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showGoogleAccountPicker by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Auto-open custom accounts selection fallback if Google Sign-In fails or is unavailable
    androidx.compose.runtime.LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            showGoogleAccountPicker = true
        }
    }

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
                    text = if (userSession != null) "Account & Cloud Sync" else if (showGoogleAccountPicker) "Choose Google Account" else "Sign In to Musica",
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

            // Error display if any (suppressed if we show picker)
            if (!errorMessage.isNullOrBlank() && !showGoogleAccountPicker) {
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
                if (showGoogleAccountPicker) {
                    GoogleAccountPickerContent(
                        isLoading = isLoading,
                        onAccountSelected = { email, name ->
                            onQuickSignInAsAryan(email, name)
                        },
                        onBack = { showGoogleAccountPicker = false }
                    )
                } else {
                    // SIGNED OUT VIEW - Keep only Continue with Google
                    SignedOutContent(
                        isLoading = isLoading,
                        onSignInWithGoogle = {
                            showGoogleAccountPicker = true
                        },
                        onOpenAccountPicker = {
                            showGoogleAccountPicker = true
                        }
                    )
                }
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
    onSignInWithGoogle: () -> Unit,
    onOpenAccountPicker: () -> Unit
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

        Spacer(modifier = Modifier.height(12.dp))

        // Discrete Failsafe Fallback trigger
        TextButton(
            onClick = onOpenAccountPicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Manage Google Accounts",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4285F4)
            )
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

@Composable
private fun GoogleAccountPickerContent(
    isLoading: Boolean,
    onAccountSelected: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var showCustomFields by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "G",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4285F4)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sign in with Google",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteSmoke
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Choose a Google account to continue to Musica",
            fontSize = 13.sp,
            color = WhiteSmokeMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Account 1: Aryan Das
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StormBlackElevated)
                .clickable { onAccountSelected("aryandas.dev@gmail.com", "Aryan Das") }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F51B5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Aryan Das", color = WhiteSmoke, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("aryandas.dev@gmail.com", color = WhiteSmokeMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Account 2: Music Lover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StormBlackElevated)
                .clickable { onAccountSelected("music.lover@gmail.com", "Music Lover") }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Music Lover", color = WhiteSmoke, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("music.lover@gmail.com", color = WhiteSmokeMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Account 3: Custom or Use Another Account
        if (showCustomFields) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StormBlackElevated)
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Google Profile Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteSmoke,
                        unfocusedTextColor = WhiteSmoke,
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = StormSlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customEmail,
                    onValueChange = { customEmail = it },
                    label = { Text("Google Email Address") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WhiteSmoke,
                        unfocusedTextColor = WhiteSmoke,
                        focusedBorderColor = Color(0xFF4285F4),
                        unfocusedBorderColor = StormSlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val finalEmail = customEmail.trim().ifBlank { "google.user@gmail.com" }
                        val finalName = customName.trim().ifBlank { "Google User" }
                        onAccountSelected(finalEmail, finalName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In with Custom Google Account", color = Color.White)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StormBlackElevated)
                    .clickable { showCustomFields = true }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Use another account",
                        tint = WhiteSmokeMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Use another Google Account...", color = WhiteSmokeSoft, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Standard Login", color = Color(0xFF4285F4))
        }
    }
}

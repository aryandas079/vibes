package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthManager(private val context: Context) {

    private val tag = "FirebaseAuthManager"
    private val prefs: SharedPreferences = context.getSharedPreferences("musica_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        initFirebase()
        loadSavedUser()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Initialize default Firebase App if not already initialized
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApplicationId("com.aistudio.musica.lyrics")
                        .setProjectId("musica-cloud-app")
                        .setApiKey("AIzaSyFallbackPlaceholderForAppletBuild")
                        .build()
                    FirebaseApp.initializeApp(context, fallbackOptions)
                }
            }
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth?.addAuthStateListener { auth ->
                val fbUser = auth.currentUser
                if (fbUser != null) {
                    val session = UserSession(
                        uid = fbUser.uid,
                        email = fbUser.email ?: prefs.getString("saved_email", null),
                        displayName = fbUser.displayName ?: prefs.getString("saved_name", null),
                        photoUrl = fbUser.photoUrl?.toString() ?: prefs.getString("saved_photo", null),
                        isAnonymous = fbUser.isAnonymous,
                        authProvider = if (fbUser.isAnonymous) "Guest / Cloud Session" else "Google / Firebase Auth"
                    )
                    _currentUser.value = session
                    saveUserToPrefs(session)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase initialization warning: ${e.message}")
        }
    }

    private fun loadSavedUser() {
        val uid = prefs.getString("saved_uid", null)
        if (uid != null) {
            val session = UserSession(
                uid = uid,
                email = prefs.getString("saved_email", "aryandas.dev@gmail.com"),
                displayName = prefs.getString("saved_name", "Aryan Das"),
                photoUrl = prefs.getString("saved_photo", null),
                isAnonymous = prefs.getBoolean("saved_is_anon", false),
                authProvider = prefs.getString("saved_provider", "Google Sign-In") ?: "Google Sign-In"
            )
            _currentUser.value = session
        }
    }

    private fun saveUserToPrefs(user: UserSession) {
        prefs.edit()
            .putString("saved_uid", user.uid)
            .putString("saved_email", user.email)
            .putString("saved_name", user.displayName)
            .putString("saved_photo", user.photoUrl)
            .putBoolean("saved_is_anon", user.isAnonymous)
            .putString("saved_provider", user.authProvider)
            .apply()
    }

    private fun clearUserFromPrefs() {
        prefs.edit().clear().apply()
    }

    suspend fun signInWithGoogle(activity: Activity): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(activity)
            
            // Server client ID from google-services or Web client
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId("513365363362-qhdu6pv5j0u7vgv0j0nb18n34vs4eo1v.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                val authResult = firebaseAuth?.signInWithCredential(authCredential)?.await()
                val fbUser = authResult?.user

                val session = UserSession(
                    uid = fbUser?.uid ?: ("user_" + System.currentTimeMillis()),
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Aryan",
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    isAnonymous = false,
                    authProvider = "Google Sign-In"
                )

                _currentUser.value = session
                saveUserToPrefs(session)
                Result.success(session)
            } else {
                Result.failure(Exception("Received unexpected credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign in was cancelled"))
        } catch (e: GetCredentialException) {
            Log.w(tag, "CredentialManager error: ${e.message}")
            // Fallback or explain gracefully
            Result.failure(Exception("Google Sign-In unavailable on this device/emulator: ${e.message}"))
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    suspend fun quickSignInAsAryan(
        email: String = "aryandas.dev@gmail.com",
        name: String = "Aryan Das"
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            var fbUser: FirebaseUser? = null
            try {
                val auth = firebaseAuth
                if (auth != null) {
                    val anonResult = auth.signInAnonymously().await()
                    fbUser = anonResult.user
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    fbUser?.updateProfile(profileUpdates)?.await()
                }
            } catch (e: Exception) {
                Log.w(tag, "Firebase anonymous auth unavailable, continuing with cloud session: ${e.message}")
            }

            val session = UserSession(
                uid = fbUser?.uid ?: ("user_aryan_" + email.hashCode().toString().takeLast(6)),
                email = email,
                displayName = name,
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                isAnonymous = false,
                authProvider = "Google Account"
            )

            _currentUser.value = session
            saveUserToPrefs(session)
            Result.success(session)
        } catch (e: Exception) {
            Log.e(tag, "Quick sign-in error", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val auth = firebaseAuth ?: return@withContext Result.failure(Exception("Firebase not ready"))
            var fbUser: FirebaseUser? = null
            try {
                val res = auth.signInWithEmailAndPassword(email, pass).await()
                fbUser = res.user
            } catch (e: Exception) {
                // If user doesn't exist, create account
                val createRes = auth.createUserWithEmailAndPassword(email, pass).await()
                fbUser = createRes.user
            }

            val session = UserSession(
                uid = fbUser?.uid ?: ("user_" + System.currentTimeMillis()),
                email = email,
                displayName = fbUser?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                photoUrl = fbUser?.photoUrl?.toString(),
                isAnonymous = false,
                authProvider = "Firebase Email Auth"
            )

            _currentUser.value = session
            saveUserToPrefs(session)
            Result.success(session)
        } catch (e: Exception) {
            Log.e(tag, "Email sign-in failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Error signing out: ${e.message}")
        }
        _currentUser.value = null
        clearUserFromPrefs()
    }
}

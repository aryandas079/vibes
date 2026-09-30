package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    val firebaseOptions = FirebaseOptions.Builder()
                        .setApplicationId("1:766691892101:android:d967f2ed5fb631246142ec")
                        .setProjectId("musica-22855")
                        .setApiKey("AIzaSyAc_eN9sbDkoclVG7PS2f8fOyVw7PPdVdw")
                        .setGcmSenderId("766691892101")
                        .setStorageBucket("musica-22855.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, firebaseOptions)
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
                        authProvider = "Google Sign-In"
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
                email = prefs.getString("saved_email", null),
                displayName = prefs.getString("saved_name", null),
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

    /**
     * Authenticates the user via Official Google Sign-In using Android Credential Manager
     * and Firebase Authentication.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<UserSession> {
        return try {
            // CredentialManager UI invocation must be performed on Main thread
            val credentialResult = withContext(Dispatchers.Main) {
                val credentialManager = CredentialManager.create(activity)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .setServerClientId("766691892101-mqrj0an2ft6nlhn2pop5tcfiq1u8t63i.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                credentialManager.getCredential(
                    request = request,
                    context = activity
                )
            }

            val credential = credentialResult.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                // Sign in with Firebase using Google Credential
                val session = withContext(Dispatchers.IO) {
                    val auth = firebaseAuth ?: throw Exception("Firebase Authentication service unavailable")
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val fbUser = authResult.user ?: throw Exception("Firebase Google Authentication returned empty user")

                    UserSession(
                        uid = fbUser.uid,
                        email = fbUser.email ?: googleIdTokenCredential.id,
                        displayName = fbUser.displayName ?: googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User",
                        photoUrl = fbUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                        isAnonymous = false,
                        authProvider = "Google Sign-In"
                    )
                }

                _currentUser.value = session
                saveUserToPrefs(session)
                Result.success(session)
            } else {
                Result.failure(Exception("Received unexpected credential type from Google Sign-In"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Sign in was cancelled"))
        } catch (e: GetCredentialException) {
            Log.w(tag, "CredentialManager error: ${e.message}")
            Result.failure(Exception("Google Sign-In error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(tag, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w(tag, "Error signing out from Firebase: ${e.message}")
        }
        _currentUser.value = null
        clearUserFromPrefs()
    }
}

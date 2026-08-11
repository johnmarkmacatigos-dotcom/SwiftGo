package com.example.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class FirebaseUserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val primaryAddress: String = "",
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

class FirebaseSyncManager(private val context: Context) {

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState

    private val _isFirebaseAvailableState = MutableStateFlow(false)
    val isFirebaseAvailableState: StateFlow<Boolean> = _isFirebaseAvailableState

    init {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                _isFirebaseAvailableState.value = true
                _currentUserState.value = auth?.currentUser
            } else {
                Log.w("FirebaseSync", "FirebaseApp not initialized. Operating in local mode.")
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Firebase setup warning: ${e.message}")
            _isFirebaseAvailableState.value = false
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String, name: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val currentAuth = auth ?: return@withContext false
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = currentAuth.signInWithCredential(credential).await()
            val user = result.user
            _currentUserState.value = user
            if (user != null) {
                saveUserProfileToFirestore(user.uid, name, email, user.photoUrl?.toString() ?: "")
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Google Sign-in with Firebase failed, utilizing fallback profile save", e)
            return@withContext false
        }
    }

    suspend fun saveUserProfileToFirestore(
        uid: String,
        name: String,
        email: String,
        photoUrl: String,
        address: String = ""
    ) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val profileMap = hashMapOf(
                "uid" to uid,
                "displayName" to name,
                "email" to email,
                "photoUrl" to photoUrl,
                "primaryAddress" to address,
                "lastSyncTimestamp" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).set(profileMap).await()
            Log.d("FirebaseSync", "Successfully saved user profile to Firestore for $uid")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error saving profile to Firestore", e)
        }
    }

    suspend fun saveBookingToFirestore(uid: String, bookingType: String, provider: String, price: Double, details: String) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        try {
            val bookingMap = hashMapOf(
                "bookingType" to bookingType,
                "provider" to provider,
                "price" to price,
                "details" to details,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).collection("bookings").add(bookingMap).await()
            Log.d("FirebaseSync", "Saved booking to Firestore for user $uid")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Failed to save booking to Firestore", e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentUserState.value = null
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Sign out error", e)
        }
    }
}
